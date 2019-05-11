package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.object.*;
import org.sarge.textrpg.util.Percentile;
import org.springframework.stereotype.Component;

/**
 * Loader for links.
 * @author Sarge
 */
@Component
public class LinkLoader {
	private static final Converter<Relationship> FACTION_LEVEL = Converter.enumeration(Relationship.class);

	private final DefaultObjectDescriptorLoader loader;

	/**
	 * Constructor.
	 * @param loader		Object registry/loader for links with a controller, e.g. portals
	 */
	public LinkLoader(DefaultObjectDescriptorLoader loader) {
		this.loader = notNull(loader);
	}

	/**
	 * Loads a link.
	 * @param xml 			Link XML
	 * @param parent		Parent for link controllers
	 * @param def			Default route
	 * @return Link descriptor
	 */
	public Link load(Element xml, Parent parent, LoaderContext ctx) {
		final String type = xml.name();
		final Route def = ctx.route();
		switch(type) {
		case "link":
			return Link.DEFAULT;

		case "route":
			return RouteLink.of(xml.attribute("route").toValue(def, Route.CONVERTER));

		case "extended":
			return new ExtendedLink(loadProperties(xml, def));

		case "hidden":
			return new HiddenLink(
				loadProperties(xml, def),
				xml.attribute("name").toText(),
				xml.attribute("vis").toValue(Percentile.CONVERTER)
			);

		case "fake":
			return new FakeLink(
				xml.attribute("name").toText(),
				xml.attribute("reason").toText()
			);

		case "portal":
			// Create and register portal
			final Portal.Descriptor descriptor = loadDescriptor(xml, Portal.Descriptor.class, "portal", "door");
			final Portal portal = descriptor.create();
			xml.attribute("ref").optional().ifPresent(ref -> ctx.add(ref, portal));

			// Create link
			final Route override = xml.attribute("route").isPresent() ? def : Route.NONE;
			return new PortalLink(loadProperties(xml, override), portal);

		case "gate":
			return loadGateLink(xml, ctx);

		case "slope":
			return new SlopeLink(
				loadProperties(xml, def),
				xml.attribute("up").toBoolean(true)
			);

		case "climb":
			final WorldObject climb = loader.load(xml.child(), ObjectDescriptorLoader.Policy.FIXTURE).create();
			climb.parent(parent);
			return new ClimbLink(
				loadProperties(xml, def),
				xml.attribute("up").toBoolean(true),
				climb,
				xml.attribute("quiet").toBoolean(true),
				xml.attribute("diff").toValue(Percentile.CONVERTER)
			);

		case "rope":
			final Rope.Anchor anchor = new Rope.Anchor(
				xml.attribute("anchor").toText(),
				xml.attribute("placement").toText(ObjectDescriptor.Characteristics.PLACEMENT_DEFAULT)
			);
			anchor.parent(parent);
			return new Rope.RopeLink(loadProperties(xml, def), anchor);

		case "blocked":
			final WorldObject blockage = loader.load(xml.child(), ObjectDescriptorLoader.Policy.FIXTURE).create();
			blockage.parent(parent);
			return new BlockedLink(loadProperties(xml, def), blockage);

		case "container":
			return new ContainerLink(
				loadProperties(xml, def),
				xml.attribute("name").toText(),
				xml.attribute("cat").toText()
			);

		default:
			throw xml.exception("Unknown link type: " + type);
		}
	}

	/**
	 * Loads link properties.
	 */
	private static ExtendedLink.Properties loadProperties(Element xml, Route def) {
		final Size size = xml.attribute("size").toValue(Size.NONE, Size.CONVERTER);
		final Route route = xml.attribute("route").toValue(def, Route.CONVERTER);
		final float mod = xml.attribute("mod").toFloat(1f);
		final String message = xml.attribute("message").optional(Converter.STRING).orElse(null);
		return new ExtendedLink.Properties(size, route, mod, message);
	}

	/**
	 * Loads a gate link.
	 * @param xml XML
	 * @return Gate link
	 */
	private Link loadGateLink(Element xml, LoaderContext ctx) {
		// Load gate controlling faction
		final Faction faction = ctx.faction().orElseThrow(() -> xml.exception("No faction for gate"));
		final Relationship relationship = xml.attribute("relationship").toValue(Relationship.FRIENDLY, FACTION_LEVEL);

		// Create gate-keeper
		final String name = xml.attribute("keeper").toText("gate.keeper");
		final Integer bribe = xml.attribute("bribe").optional(Converter.INTEGER).orElse(null);
		final Gate.Keeper keeper = new Gate.Keeper(name, new Faction.Association(faction, relationship), bribe);

		// Create gate
		final ObjectDescriptor descriptor = loadDescriptor(xml, ObjectDescriptor.class, "gate", "gate");
		final Gate.Descriptor gate = new Gate.Descriptor(descriptor, keeper);

		// Register open/close listener
		faction.opening().add(period -> gate.setOpen(period.isOpen()));
		gate.setOpen(faction.opening().current().isOpen());
		// TODO - not much point in listener on descriptor since we create a new one every time?

		// Create gate and link
		return gate.create().link();
	}

	/**
	 * Helper - Loads an object descriptor either from a custom definition or a pre-defined type.
	 * @param xml			XML
	 * @param type			Expected type
	 * @param child			Child element name
	 * @param def			Default name
	 * @return Descriptor
	 */
	private <T extends ObjectDescriptor> T loadDescriptor(Element xml, Class<T> type, String child, String def) {
		// Load custom descriptor or use default
		// TODO - messy and doesn't fully work!
		final ObjectDescriptor descriptor = xml.find(child)
			.map(e -> loader.load(e, ObjectDescriptorLoader.Policy.FIXTURE))
			.or(() -> Optional.ofNullable(loader.get(def)))
			.orElseGet(() -> loader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE)); // TODO - will never get used (cos of previous line)?

		// Verify correct type
		if(descriptor.getClass() == type) {
			return type.cast(descriptor);
		}
		else {
			throw xml.exception(String.format("Invalid descriptor: expected=%s actual=%s", type, descriptor.getClass()));
		}
	}
}
