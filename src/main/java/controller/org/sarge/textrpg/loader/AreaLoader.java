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

import org.sarge.lib.collection.Pair;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.ConverterAdapter;
import org.sarge.lib.util.StreamUtil;
import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.ElementLoader;
import org.sarge.textrpg.common.LoaderException;
import org.sarge.textrpg.entity.Race;
import org.sarge.textrpg.object.LootFactory;
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
	private static final Predicate<Element> FILTER = StreamUtil.not(node -> NAMES.contains(node.name()));

	private static final ElementLoader LOADER = new ElementLoader();

	/**
	 * Helper - Opens an XML file.
	 */
	private static Element open(Path path) {
		try {
			return LOADER.load(Files.newBufferedReader(path));
		}
		catch(final IOException e) {
			throw new RuntimeException("Error opening file: " + path, e);
		}
	}

	private final LocationLoader locationLoader;
	private final LinkLoader linkLoader;
	private final World world;
	private final Race def;

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
	 * Loader pass.
	 */
	private interface Pass {
		Area load(Element node, Area parent, Terrain terrain, Route route);
	}

	/**
	 * Location loader pass.
	 */
	private final Pass locationPass = (node, parent, parentTerrain, parentRoute) -> {
		// Load area
		final ConverterAdapter attrs = node.attributes();
		final String name = attrs.toString("name");

		// Load transient area properties
		final Terrain terrain = attrs.toValue("default-terrain", parentTerrain, TERRAIN);
		final Route route = attrs.toValue("default-route", parentRoute, ROUTE);

		// Load resources in this area
		final Map<Resource, LootFactory> resources = node.children("resource").map(AreaLoader.this::loadResourceFactory).collect(Pair.toMap());

		// Load ambient events
		final Collection<Ambient> ambient = node.children("ambient").map(AreaLoader::loadAmbient).collect(toList());

		// Create area
		final Area area = new Area(name, parent, resources, ambient);
		LOG.log(Level.FINE, "Area: {0}", area.getName());

		// Load locations
		node.children().filter(FILTER).forEach(e -> loadLocation(e, area));
		return area;
	};

	/**
	 * Loads a resource factory.
	 */
	private Pair<Resource, LootFactory> loadResourceFactory(Element node) {
		final Resource res = node.attributes().toValue("res", null, RESOURCE);
		final LootFactory f = locationLoader.getLootLoader().load(node.child());
		return new Pair<>(res, f);
	}

	/**
	 * Loads an ambient event descriptor.
	 */
	private static Ambient loadAmbient(Element node) {
		final ConverterAdapter attrs = node.attributes();
		final String name = attrs.toString("name", null);
		final long period = attrs.toValue("period", null, Converter.DURATION).toMillis();
		final boolean repeat = attrs.toBoolean("repeat", true);
		return new Ambient(name, period, repeat);
	}

	private void loadLocation(Element node, Area area) {
	    locationLoader.load(node, area);
	}

	/**
	 * Links pass.
	 */
	private final Pass linksPass = (node, parent, terrain, route) -> {
		try {
			node.children().filter(FILTER).forEach(e -> AreaLoader.this.loadContents(e, route));
		}
		catch(final LoaderException e) {
			throw e;
		}
		catch(final Exception e) {
			throw node.exception(e);
		}
		return null;
	};

	/**
	 * Loads links and contents.
	 */
	private void loadContents(Element node, Route route) {
		// Retrieve location
		final String name = node.attributes().toString("name");
		final Location loc = world.getLocations().find(name);
		if(loc == null) throw new RuntimeException();

		// Load links
		node.optionalChild("links")
			.map(Element::children)
			.orElse(Stream.empty())
			.map(e -> linkLoader.loadLinkWrapper(e, route, loc))
			.forEach(loc::add);

		// Load default race
		final Race race = node.attributes().toValue("default-race", def, world.getRaces()::find);

		// Load contents
		node.optionalChild("contents")
			.map(Element::children)
			.orElse(Stream.empty())
			.forEach(e -> locationLoader.loadContents(e, race, loc));
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
		catch(final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Area load(Element node, Area parent, Pass pass) {
		final Area area = pass.load(node, parent);
		node.children("area").forEach(e -> load(e, area, pass));
		return area;
	}
}
