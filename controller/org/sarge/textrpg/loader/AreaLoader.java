package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.Pair;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.common.LoaderException;
import org.sarge.textrpg.entity.Race;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.util.TextNode;
import org.sarge.textrpg.util.TextParser;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Area.Ambient;
import org.sarge.textrpg.world.Area.Resource;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;

/**
 * Loader for an area.
 * @author Sarge
 */
public class AreaLoader {
	private static final Logger LOG = Logger.getLogger(AreaLoader.class.getName());
	
	private static final Converter<Resource> RESOURCE = Converter.enumeration(Resource.class);
	private static final Converter<Terrain> TERRAIN = Converter.enumeration(Terrain.class);
	private static final Converter<Route> ROUTE = Converter.enumeration(Route.class);

	// TODO - nasty
	private static final Collection<String> NAMES = Arrays.asList("area", "resource", "ambient");
	private static final Predicate<TextNode> FILTER = Util.not(node -> NAMES.contains(node.name()));
	
	private static final TextParser LOADER = new TextParser();
	
	/**
	 * Helper - Opens an XML file.
	 */
	private static TextNode open(Path path) {
		try {
			return LOADER.parse(Files.newBufferedReader(path));
		}
		catch(IOException e) {
			throw new RuntimeException("Error opening file: " + path, e);
		}
	}
	
	private final LocationLoader locationLoader;
	private final LinkLoader linkLoader;
	private final World world;
	private final Race def;
	
	/**
	 * Loader pass.
	 */
	private interface Pass {
		Area load(TextNode node, Area parent);
	}

	/**
	 * Location loader pass.
	 */
	private final Pass locationPass = new Pass() {
		@Override
		public Area load(TextNode node, Area parent) {
			// Load area
			final String name = node.getString("name", null);
			final Terrain terrain = node.getAttribute("default-terrain", parent.getTerrain(), TERRAIN);
			final Route route = node.getAttribute("default-route", parent.getRouteType(), ROUTE);
			
			// Load resources in this area
			final Map<Resource, LootFactory> resources = node.children("resource").map(AreaLoader.this::loadResourceFactory).collect(Pair.toMap());
			
			// Load ambient events
			final Collection<Ambient> ambient = node.children("ambient").map(AreaLoader::loadAmbient).collect(toList());
			
			// Create area
			final Area area = new Area(name, parent, terrain, route, resources, ambient);
			LOG.log(Level.FINE, "Area: {0}", area.getName());
			// TODO - terrain, route and default-race are all transient once loading is done, bind into an a context
			
			// Load locations
			node.children().filter(FILTER).forEach(e -> locationLoader.load(e, area));
			return area;
		}
	};

	/**
	 * Loads a resource factory.
	 */
	private Pair<Resource, LootFactory> loadResourceFactory(TextNode node) {
		final Resource res = node.getAttribute("res", null, RESOURCE);
		final LootFactory f = locationLoader.getLootLoader().load(node.child());
		return new Pair<>(res, f);
	}

	/**
	 * Loads an ambient event descriptor.
	 */
	private static Ambient loadAmbient(TextNode node) {
		final String name = node.getString("name", null);
		final long period = node.getAttribute("period", null, Converter.DURATION).toMillis();
		final boolean repeat = node.getBoolean("repeat", true);
		return new Ambient(name, period, repeat);
	}

	/**
	 * Links pass.
	 */
	private final Pass linksPass = (node, parent) -> {
		try {
			node.children().filter(FILTER).forEach(e -> AreaLoader.this.loadContents(e));
		}
		catch(LoaderException e) {
			throw e;
		}
		catch(Exception e) {
			throw node.exception(e);
		}
		return null;
	};
	
	/**
	 * Loads links and contents.
	 */
	private void loadContents(TextNode node) {
		// Retrieve location
		final String name = node.getString("name", null);
		final Location loc = world.getLocations().find(name);
		if(loc == null) throw new RuntimeException();
		
		// Load links
		final Route route = loc.getArea().getRouteType();
		node.optionalChild("links")
			.map(TextNode::children)
			.orElse(Stream.empty())
			.map(e -> linkLoader.loadLinkWrapper(e, route, loc))
			.forEach(loc::add);
		
		// Load default race
		final Race race = node.getAttribute("default-race", def, world.getRaces()::find);
		
		// Load contents
		node.optionalChild("contents")
			.map(TextNode::children)
			.orElse(Stream.empty())
			.forEach(e -> locationLoader.loadContents(e, race, loc));
	}
	
	/**
	 * Constructor.
	 * @param locationLoader		Locations loader
	 * @param linkLoader			Links loader
	 * @param races					Races for creature spawning
	 */
	public AreaLoader(LocationLoader locationLoader, LinkLoader linkLoader, World world) {
		Check.notNull(locationLoader);
		Check.notNull(linkLoader);
		this.locationLoader = locationLoader;
		this.linkLoader = linkLoader;
		this.world = world;
		this.def = world.getRaces().find("man");
	}
	
	/**
	 * Recursively loads areas.
	 * @param path		Starting path
	 * @param parent	Parent area
	 */
	public void load(Path path, Area parent) {
		load(path, locationPass, parent);
		load(path, linksPass, parent);
	}
	
	/**
	 * Loads a pass.
	 */
	private void load(Path dir, Pass pass, Area parent) {
		try {
			// Load root area
			final Path root = Paths.get(dir.toString(), dir.getFileName() + ".node");
			final Area area = load(open(root), parent, pass);

			// Load other files
			Files.list(dir)
				.filter(Files::isRegularFile)
				.filter(file -> !file.equals(root))
				.filter(file -> file.getFileName().toString().endsWith(".node"))
				.map(AreaLoader::open)
				.forEach(node -> load(node, area, pass));
			
			// Recurse to sub-directories
			Files.list(dir)
				.filter(Files::isDirectory)
				.forEach(file -> load(file, pass, area));
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Area load(TextNode node, Area parent, Pass pass) {
		final Area area = pass.load(node, parent);
		node.children("area").forEach(e -> load(e, area, pass));
		return area;
	}
}
