package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.sarge.lib.collection.Pair;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.ConverterAdapter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.entity.*;
import org.sarge.textrpg.entity.Race.Attributes;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ScriptLoader;
import org.sarge.textrpg.object.Shop;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.SelectFollower;
import org.sarge.textrpg.world.Terrain;

/**
 * Loader for an {@link Entity}.
 * @author Sarge
 */
public class EntityLoader {
	private final World world;
	private final Map<Size, Integer> weight;
	private final ObjectLoaderAdapter objectLoader;
	private final ScriptLoader scriptLoader;
	private final EntityValueCalculator calc;
	private final MovementController mover;

	private final Duration period = Duration.ofSeconds(30);

	/**
	 * Constructor.
	 * @param weight			Weight table
	 * @param objectLoader		Object loader for inventory and shop
	 * @param scriptLoader		Script loader
	 */
	public EntityLoader(World world, Map<Size, Integer> weight, ObjectLoaderAdapter objectLoader, ScriptLoader scriptLoader, EntityValueCalculator calc, MovementController mover) {
		Check.notNull(objectLoader);
		Check.notNull(scriptLoader);
		this.world = world;
		this.weight = weight;
		this.objectLoader = objectLoader;
		this.scriptLoader = scriptLoader;
		this.calc = notNull(calc);
		this.mover = notNull(mover);
	}

	/**
	 * Loads a creature or group.
	 * @param node	XML
	 * @param def	Default race
	 * @param loc	Location
	 * @return Entity
	 */
	@SuppressWarnings("unused")
	public void loadCreature(Element node, Race def, Location loc) {
		// Load entity race
		final Race race = node.attributes().toValue("race", def, world.getRaces()::find);

		// Load manager
		final EntityManager manager = node.optionalChild("manager").map(this::loadManager).orElse(EntityManager.IDLE);

		// Create entities
		final List<Creature> creatures = new ArrayList<>();
		final int num = node.attributes().toInteger("group", 1);
		for(int n = 0; n < num; ++n) {
			final Creature creature = new Creature(race, manager);
			init(creature, loc, n == 0);
			creatures.add(creature);
		}

		// Create group
		if(num > 1) {
			new Group(creatures);
		}
	}

	/**
	 * Loads a character.
	 * @param node XML
	 * @param def Default race
	 * @return Entity
	 */
	public CharacterEntity loadCharacter(Element node, Race def, Location loc) {
		// Lookup race
		final ConverterAdapter attrs = node.attributes();
		final Race race = attrs.toValue("race", def, world.getRaces()::find);

		// Load character attributes
		final Attributes attributes = race.getAttributes();
		final String name = attrs.toString("name", null);
		final Gender gender = attrs.toValue("gender", attributes.getDefaultGender(), LoaderHelper.GENDER);
		final Alignment align = attrs.toValue("align", attributes.getDefaultAlignment(), LoaderHelper.ALIGNMENT);
		final MutableIntegerMap<Attribute> map = new MutableIntegerMap<>(Attribute.class, race.getAttributes().getAttributes());
		LoaderHelper.loadAttributes(node, map);

		// TODO
		// - inventory
		// - skills?

		// Load conversation topics
		final Collection<Topic> topics = node.children("topic").map(e -> loadTopic(e)).collect(toList());

		// Load shop
		node.optionalChild("shop").map(this::loadShop).ifPresent(topics::add);

		// Load entity manager
		final EntityManager manager = node.optionalChild("manager").map(this::loadManager).orElse(EntityManager.IDLE);

		// Create character
		final CharacterEntity ch = new CharacterEntity(name, race, map, manager, gender, align, topics);
		init(ch, loc, true);
		return ch;
	}

	/**
	 * Initialises an entity.
	 */
	private void init(Entity e, Location loc, boolean start) {
		// Init entity stats
		calc.init(e);

		// Add to world
		e.setParentAncestor(loc);

		// Start entity-manager
		if(start) {
			e.getEntityManager().start(e);
		}
	}

	/**
	 * Loads a conversation topic.
	 */
	private Topic loadTopic(Element node) {
		final String name = node.attributes().toString("name", null);
		final Script script = scriptLoader.load(node.child());
		return new Topic(name, script);
	}

	/**
	 * Loads a shop topic.
	 */
	private Topic loadShop(Element node) {
		// Load shop descriptor
		final Function<Element, Pair<ObjectDescriptor, Integer>> mapper = child -> {
			final ObjectDescriptor descriptor = objectLoader.getDescriptors().find(child.attributes().toString("type", null));
			final int num = child.attributes().toInteger("num", null);
			return new Pair<>(descriptor, num);
		};
		final Set<String> accepts = node.children("accepts").map(Element::name).collect(toSet());
		final Map<ObjectDescriptor, Integer> stock = node.children("stock").map(mapper).collect(Pair.toMap());
		final ConverterAdapter attrs = node.attributes();
		final int mod = attrs.toInteger("repair", 1);
		final long duration = attrs.toLong("duration", 60L);
		final long discard = attrs.toLong("discard", 60L);
		final Shop shop = new Shop(accepts, stock, duration, mod, discard);

		// Load opening times
		LoaderHelper.loadToggleListener(node, shop::setOpen);

		// Load topic
		final String text = attrs.toString("topic", "shop.topic.default");
		return shop.topic(text);
	}

	/**
	 * Loads an entity-manager.
	 */
	private EntityManager loadManager(Element node) {
		final String type = node.attributes().toString("type", "repeat");
		switch(type) {
		case "repeat":
			final long period = node.attributes().toValue("period", this.period, Converter.DURATION).toMillis();
			final EntityManager.Action action = loadAction(node);
			return new RepeatEntityManager(action, period);

		case "periodic":
			// TODO

		default:
			throw node.exception("Invalid entity-manager type: " + type);
		}
	}

	/**
	 * Loads an entity-manager action.
	 */
	private EntityManager.Action loadAction(Element node) {
		final ConverterAdapter attrs = node.attributes();
		final String type = attrs.toString("action", "move");
		switch(type) {
		case "move":
			// Load predicate
			final Predicate<Exit> filter = loadMoveFilter(node);

			// Create follower
			final SelectFollower follower = new SelectFollower(filter, SelectFollower.Policy.RANDOM);
			follower.setAllowRetrace(attrs.toBoolean("retrace", true));
			follower.setBound(attrs.toBoolean("bound", true));

			// Create move action
			final boolean stop = attrs.toBoolean("stop", false);
			return new MoveEntityAction(follower, mover, stop);

		case "path":
			// TODO - path follower

		default:
			throw node.exception("Invalid entity-manager action: " + type);
		}
	}

	/**
	 * Loads a movement filter.
	 */
	private static Predicate<Exit> loadMoveFilter(Element node) {
		// Terrain filter
		final Set<Terrain> terrain = LoaderHelper.loadEnumeration(node, "terrain", LoaderHelper.TERRAIN);
		if(!terrain.isEmpty()) return SelectFollower.terrain(terrain);

		// Route filter
		final Set<Route> routes = LoaderHelper.loadEnumeration(node, "routes", LoaderHelper.ROUTE);
		if(!routes.isEmpty()) return SelectFollower.route(routes);

		// No filter
		return exit -> true;
	}
}
