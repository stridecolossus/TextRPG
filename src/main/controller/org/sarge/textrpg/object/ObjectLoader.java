package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.function.Function;

import org.sarge.lib.collection.LoopIterator;
import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.entity.Ferry;
import org.sarge.textrpg.entity.FerryController;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.world.LoaderContext;
import org.sarge.textrpg.world.Location;
import org.springframework.stereotype.Service;

/**
 * Loader for objects in a location.
 * @author Sarge
 */
@Service
public class ObjectLoader {
	private static final Converter<WorldObject.Interaction> INTERACTION = Converter.enumeration(WorldObject.Interaction.class);
	private static final Converter<Control.Policy> CONTROL_POLICY = Converter.enumeration(Control.Policy.class);
	private static final Converter<Openable.State> OPENABLE_STATE = Converter.enumeration(Openable.State.class);
	private static final Converter<LoopIterator.Strategy> STRATEGY = Converter.enumeration(LoopIterator.Strategy.class);

	private final DefaultObjectDescriptorLoader loader;
	private final OpenableLockLoader lockLoader;
	private final FerryController controller;

	/**
	 * Constructor.
	 * @param loader 			Loader/registry for object descriptors
	 * @param lockLoader		Loader/registry for locks and keys
	 * @param controller		Controller for ferries
	 */
	public ObjectLoader(DefaultObjectDescriptorLoader loader, OpenableLockLoader lockLoader, FerryController controller) {
		this.loader = notNull(loader);
		this.lockLoader = notNull(lockLoader);
		this.controller = notNull(controller);
	}

	/**
	 * Loads an object.
	 * @param xml XML
	 * @return Object
	 */
	public WorldObject load(Element xml, LoaderContext ctx) {
		switch(xml.name()) {
		case "dispenser":		return loadDispenser(xml);
		case "control":			return loadControl(xml, ctx);
		case "ferry":			return loadFerry(xml, ctx);
		default:				return loadFixture(xml, ctx);
		}
	}

	/**
	 * Loads a fixture object.
	 * @param xml XML
	 * @param ctx Context
	 * @return Fixture
	 */
	private WorldObject loadFixture(Element xml, LoaderContext ctx) {
		// Load/lookup descriptor
		final ObjectDescriptor descriptor = loader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE);
		final WorldObject obj = descriptor.create();

		// Register object
		xml.attribute("ref").optional().ifPresent(ref -> ctx.add(ref, obj));

		// Load container contents
		if(obj instanceof Container) {
			loadContents(xml, ctx, (Container) obj);
		}

		return obj;
	}

	/**
	 * Loads container contents.
	 */
	private void loadContents(Element xml, LoaderContext ctx, Container container) {
		xml.find("contents").stream()
			.flatMap(Element::children)
			.map(e -> load(e, ctx))
			.forEach(t -> t.parent(container));
	}

	/**
	 * Loads a control.
	 * @param xml XML
	 * @param ctx Context
	 * @return Control
	 */
	private WorldObject loadControl(Element xml, LoaderContext ctx) {
		// Load control descriptor
		final ObjectDescriptor descriptor = loader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE);

		// Load handler
		final Control.Handler handler = loadHandler(xml, ctx);

		// Create control
		final WorldObject.Interaction interaction = xml.attribute("interaction").toValue(WorldObject.Interaction.PUSH, INTERACTION);
		final Control.Policy policy = xml.attribute("policy").toValue(Control.Policy.DEFAULT, CONTROL_POLICY);
		return new Control(descriptor, interaction, handler, policy);
	}

	/**
	 * Loads a control handler.
	 */
	private Control.Handler loadHandler(Element xml, LoaderContext ctx) {
		final String type = xml.attribute("handler").toText();
		switch(type) {
		case "reveal":
			// Load fixture reveal handler
			final WorldObject fixture = loadHandlerTarget(xml, ctx);
			return RevealControlHandler.fixture(fixture);

		case "factory":
			// Load factory reveal handler
			final ObjectDescriptor descriptor = loader.load(xml, ObjectDescriptorLoader.Policy.OBJECT);
			return RevealControlHandler.factory(descriptor);

		case "openable":
			// Load openable handler
			final WorldObject obj = loadHandlerTarget(xml, ctx);
			if(!(obj instanceof Openable)) throw xml.exception("");
			final Openable.State state = xml.attribute("state").toValue(Openable.State.OPEN, OPENABLE_STATE);
			final String message = xml.attribute("message").toText();
			return new OpenableControlHandler((Openable) obj, state, message);

		case "count":
			// Load counting control handler
			final int max = xml.attribute("count").toInteger();
			return new CountControlHandler(loadHandler(xml.child(), ctx), max);

		default:
			throw xml.exception("Invalid control handler type: " + type);
		}
	}

	/**
	 * Loads a control handler target.
	 */
	private static WorldObject loadHandlerTarget(Element xml, LoaderContext ctx) {
		final String name = xml.attribute("target").toText();
		final WorldObject obj = ctx.object(name);
		if(obj == null) throw xml.exception("Unknown control target: " + name);
		return obj;
	}

	/**
	 * Loads an object dispenser.
	 */
	private WorldObject loadDispenser(Element xml) {
		// Load descriptor for dispensed object
		final ObjectDescriptor descriptor;
		final Element child = xml.child();
		if(child.name().equals("key")) {
			// Lookup/create key descriptor
			descriptor = lockLoader.key(child.attribute("name").toText());
		}
		else {
			// Load dispensed object descriptor
			descriptor = loader.load(child, ObjectDescriptorLoader.Policy.OBJECT);
		}

		// Create dispenser adapter
		final int max = xml.attribute("max").toInteger(1);
		final Duration refresh = xml.attribute("refresh").toValue(DurationConverter.CONVERTER);
		final Dispenser dispenser = new Dispenser(descriptor, max, refresh);

		// Create dispensed object
		// TODO - this should be N objects?
		return dispenser.create();
	}

	/**
	 * Loads a ferry.
	 * @param xml XML
	 * @param ctx Context
	 * @return Ferry
	 */
	private Ferry loadFerry(Element xml, LoaderContext ctx) {
		// Load ferry properties
		final String name = xml.attribute("name").toText();
		final boolean ticket = xml.attribute("ticket").toBoolean(false);

		// Load way-points
		final Function<Element, Location> loader = e -> {
			final String dest = xml.attribute("dest").toText();
			final Location loc = ctx.junction(dest); // TODO - would only work within an area?
			if(loc == null) throw xml.exception("Unknown ferry destination: " + dest);
			return loc;
		};
		final var waypoints = xml.children("waypoint").map(loader).collect(toList());

		// Create ferry
		final Ferry ferry = new Ferry(name, waypoints, ticket, LimitsMap.EMPTY); // TODO - limits

		// Load controller for automated ferries
		if(xml.attribute("automated").toBoolean(false)) {
			final LoopIterator.Strategy strategy = xml.attribute("strategy").toValue(LoopIterator.Strategy.CYCLE, STRATEGY);
			final Duration period = xml.attribute("duration").toValue(DurationConverter.CONVERTER);
			controller.start(ferry, strategy, period);
		}

		return ferry;
	}
}
