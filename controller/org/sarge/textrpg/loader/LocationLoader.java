package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.MapBuilder;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.entity.CharacterEntity;
import org.sarge.textrpg.entity.Race;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectFactory;
import org.sarge.textrpg.object.Portal;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.TextNode;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.FloorlessLocation;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.MoveableLocation;
import org.sarge.textrpg.world.MoveableLocation.Stage;
import org.sarge.textrpg.world.Terrain;

/**
 * Loader for a {@link Location}.
 * @author Sarge
 */
public class LocationLoader {
	private static final Logger LOG = Logger.getLogger(LocationLoader.class.getName());
	private static final Duration DEFAULT_RESET = Duration.ofMinutes(5);
	
	/**
	 * Convenience location types mapped by terrain.
	 */
	private static final Map<String, Terrain> TERRAIN_MAP = new MapBuilder<String, Terrain>()
		.add("room", Terrain.INDOORS)
		.add("tunnel", Terrain.UNDERGROUND)
		.build();
	
	private final ObjectLoaderAdapter objectLoader;
	private final LootFactoryLoader lootLoader;
	private final EntityLoader entityLoader;
	private final World world;

	/**
	 * Constructor.
	 * @param objectLoader		Loader for objects
	 * @param entityLoader		Loader for entities
	 * @param world				World model
	 */
	public LocationLoader(ObjectLoaderAdapter objectLoader, LootFactoryLoader lootLoader, EntityLoader entityLoader, World world) {
		Check.notNull(objectLoader);
		Check.notNull(entityLoader);
		this.objectLoader = objectLoader;
		this.lootLoader = lootLoader;
		this.entityLoader = entityLoader;
		this.world = world;
	}
	
	public LootFactoryLoader getLootLoader() {
		return lootLoader;
	}
	
	/**
	 * Loads a location.
	 * @param node		Text-node
	 * @param area		Parent area
	 * @return Location
	 */
	public Location load(TextNode node, Area area) {
		// Load location
		final Location loc = loadLocation(node, area);
		
		// Load sub-class
		final Location sub = loadLocation(node, loc);

		// Register
		LOG.log(Level.FINE, sub.getName());
		world.getLocations().add(sub);
		return sub;
	}

	/**
	 * Loads location descriptor.
	 */
	private static Location loadLocation(TextNode node, Area area) {
		final String name = node.getString("name", null);
		final Terrain terrain = loadTerrain(node, area.getTerrain());
		final boolean water = node.getBoolean("water", false);
		final Collection<String> decorations = node.children("decoration").map(TextNode::name).collect(toSet());
		return new Location(name, area, terrain, water, decorations);
	}

	/**
	 * Loads terrain by location name.
	 */
	private static Terrain loadTerrain(TextNode node, Terrain def) {
		final Terrain terrain = TERRAIN_MAP.get(node.name());
		if(terrain == null) {
			return node.getAttribute("terrain", def, LoaderHelper.TERRAIN);
		}
		else {
			return terrain;
		}
	}

	/**
	 * Loads a location sub-class.
	 */
	private Location loadLocation(TextNode node, Location loc) {
		// Load sub-class
		final String type = node.name();
		switch(type) {
		case "location":
		case "room":
		case "tunnel":
			return loc;
			
		case "floorless":
			final String name = node.getString("base", null);
			final Location base = world.getLocations().find(name);
			return new FloorlessLocation(loc, base);
			
		case "moveable":
			final boolean ferry = node.getBoolean("ferry", true);
			final List<Stage> stages = node.children("location").map(e -> loadStage(e, loc.getArea())).collect(toList());
			if(stages.size() < 2) throw node.exception("Not enough stages");
			return new MoveableLocation(loc.getName(), stages, ferry);
			
		default:
			throw node.exception("Invalid location type: " + type);
		}
	}
	
	/**
	 * Loads a moveable location stage.
	 */
	private Stage loadStage(TextNode node, Area area) {
		// Check no links for this fake location
		if(node.children().count() > 0) throw node.exception("Cannot specify contents of a location stage");
		
		// Load stage location
		if(!node.name().equals("location")) throw node.exception("Stages can only be basic locations");
		final Location loc = loadLocation(node, area);
		
		// Create stage
		final long period = node.getAttribute("period", null, Converter.DURATION).toMillis();
		final Stage stage = new Stage(loc, period);

		// Register stage
		LOG.log(Level.FINE, stage.getName());
		world.getLocations().add(stage);
		return stage;
	}

	/**
	 * Loads an object or entity.
	 * @param def Default race
	 */
	protected void loadContents(TextNode node, Race def, Location loc) {
		switch(node.name()) {
		case "creature":
			entityLoader.loadCreature(node, def, loc);
			break;
			
		case "character":
			final CharacterEntity ch = entityLoader.loadCharacter(node, def, loc);
			world.getCharacters().add(ch);
			break;
		
		case "factory":
			loadFactory(node, loc);
			break;
			
		case "factory-container":
			final ObjectDescriptor descriptor = objectLoader.loadDescriptor(node);
			final Container.Descriptor container = objectLoader.getObjectLoader().loadContainer(node, descriptor);
			final Container c = (Container) container.toFixture().create();
			c.setParentAncestor(loc);
			loadFactory(node.child(), c);
			break;
			
		default:
			// Load fixture
			final WorldObject obj = objectLoader.loadObject(node, false);
			if(obj.getDescriptor() instanceof Portal.Descriptor) throw node.exception("Cannot create a portal directly");
			obj.setParentAncestor(loc);
			break;
		}
	}
	
	/**
	 * Loads an object-factory.
	 */
	@SuppressWarnings("unused")
	private void loadFactory(TextNode node, Parent parent) {
		// Load loot-factory
		final LootFactory factory;
		final String type = node.getValue("type");
		if(type == null) {
			factory = lootLoader.load(node);
		}
		else {
			final ObjectDescriptor descriptor = world.getDescriptors().find(type);
			if(descriptor == null) throw node.exception("Unknown descriptor: " + type);
			factory = LootFactory.object(descriptor, 1);
		}
		
		// Create object factory
		final int count = node.getInteger("count", 1);
		final long period = node.getAttribute("reset", DEFAULT_RESET, Converter.DURATION).toMillis();
		new ObjectFactory(factory, parent, count, period);
	}
}
