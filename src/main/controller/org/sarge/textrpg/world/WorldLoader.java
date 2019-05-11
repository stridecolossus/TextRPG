package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.Element.ElementException;
import org.sarge.lib.xml.ElementLoader;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.NameStoreLoader;
import org.sarge.textrpg.util.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * World loader.
 * <p>
 * The <i>world</i> loader recursively walks the world map directory and loads all areas, locations and corresponding name-stores.
 * <p>
 * Note that each sub-folder represents a child area and is assumed to have at least the <i>root</i> area descriptor (see below).
 * <p>
 * Each folder consists of:
 * <ul>
 * <li>The <i>root</i> area descriptor XML with the same filename as that of the folder</li>
 * <li>None-or-more area XML files for additional sub-areas</li>
 * <li>One-or-more name-store <tt>.properties</tt> files</li>
 * </ul>
 * Notes:
 * <ul>
 * <li>Other files in a folder are ignored (and logged as warnings)</li>
 * <li>Default area properties are inherited by sub-areas (terrain, route, etc)</li>
 * <p>
 * @author Sarge
 */
@Component
@ConditionalOnBean(name={"skills", "descriptors", "races"})
// TODO - very coupled to Spring
// TODO - nicer to move this to an app runner?
public class WorldLoader {
	private static final Logger LOG = LoggerFactory.getLogger(WorldLoader.class);

	@Component
	private static class LocationLoaders {
		@Autowired private DefaultLocationLoader location;
		@Autowired private GridLoader grid;
		@Autowired private PathLoader path;
	}

	@Value("${data.source}") private Path root;			// TODO - should be from config?

	@Autowired private AreaLoader areaLoader;
	@Autowired private LocationLoaders loaders;
	@Autowired private Location.Linker linker;
	@Autowired private FactionLoader factionLoader;

	private final Registry.Builder<Faction> factions = new Registry.Builder<>(Faction::name);

	@Bean
	@ConditionalOnBean(WorldLoader.class)
	private Registry<Faction> factions() {
		return factions.build();
	}

	/**
	 * Loads the world.
	 * @throws IOException if the world cannot be loaded
	 */
	@PostConstruct
	public void load() throws IOException {
		// Load world map
		LOG.info("Loading world map...");
		final Path path = root.resolve("world");
		final Loader loader = new Loader(path);
		Files.walk(path).filter(Files::isDirectory).forEach(loader::load);
		loader.stack.pop();

		// Link locations
		LOG.info("Linking locations...");
		linker.link();
	}

	/**
	 * Directory walker.
	 */
	private class Loader {
		private final ElementLoader loader = new ElementLoader();
		private final LoaderContext.Stack stack = new LoaderContext.Stack();
		private final int prefix;

		/**
		 * Constructor.
		 * @param root Root directory
		 */
		public Loader(Path root) {
			prefix = root.getNameCount();
			stack.push(new LoaderContext(Area.ROOT, Terrain.SCRUBLAND, Route.NONE, null));
		}

		/**
		 * Loads areas and locations from the given directory.
		 * @param dir Directory
		 */
		private void load(Path dir) {
			LOG.info("Loading " + dir);

			// Trim stack to this directory level
			final int diff = (stack.size() - 1) - (dir.getNameCount() - prefix);
			assert diff >= 0;
			if(diff > 0) {
				for(int n = 0; n < diff; ++n) {
					stack.pop();
				}
			}

			// Load area
			final Path root = dir.resolve(dir.getFileName() + ".xml");
			try {
				final LoaderContext area = loadArea(root, stack.parent());
				stack.push(area);
				loadChildAreas(dir, area, root);
			}
			catch(Exception e) {
				throw new RuntimeException(e.getMessage() + " in " + root.toString(), e);
			}
		}

		/**
		 * Loads an area, locations and any corresponding name-store.
		 * @param file File-path
		 * @throws IOException if the area cannot be loaded
		 */
		private LoaderContext loadArea(Path file, LoaderContext ctx) throws IOException {
			// Load name-store for this area
			LOG.info("Loading " + file);
			final NameStore store = loadStore(file);

			// Load XML
			final Element xml = loader.load(Files.newBufferedReader(file));

			// Load area
			final Area area = areaLoader.load(xml, ctx.area(), store);

			// Load area defaults
			final Terrain terrain = xml.attribute("terrain").toValue(ctx.terrain(), Terrain.CONVERTER);
			final Route route = xml.attribute("route").toValue(ctx.route(), Route.CONVERTER);

			// Load faction
			final Faction faction = xml.find("faction").map(e -> factionLoader.load(e, area)).orElse(null);
			if(faction != null) {
				factions.add(faction);
			}

			// Wrap area
			final LoaderContext wrapper = new LoaderContext(area, terrain, route, faction);

			// Load locations
			xml.children().forEach(e -> loadAreaContents(e, wrapper));

			// TODO - contents!
			return wrapper;
		}

		/**
		 * Loads other areas in the given directory.
		 * @param dir Directory
		 * @throws IOException if the areas cannot be loaded
		 */
		private void loadChildAreas(Path dir, LoaderContext parent, Path root) throws IOException {
			// Enumerate other areas
			final var files = Arrays.stream(dir.toFile().listFiles())
				.filter(f -> f.getName().endsWith(".xml"))
				.map(File::toPath)
				.collect(toList());

			// Skip root area
			files.remove(root);

			// Load areas
			for(Path p : files) {
				try {
					loadArea(p, parent);
				}
				catch(ElementException e) {
					e.setFile(p.toString());
					throw e;
				}
			}
		}

		/**
		 * Loads a name-store.
		 * @param path File-path
		 * @return Name-store
		 * @throws IOException if the name-store cannot be loaded
		 */
		private NameStore loadStore(Path path) throws IOException {
			final String filename = path.getFileName().toString();
			final int index = filename.indexOf('.');
			final Path file = path.resolveSibling(filename.substring(0, index) + ".properties");
			if(file.toFile().exists()) {
				LOG.info("Loading " + file);
				final NameStoreLoader loader = new NameStoreLoader();
				loader.load(Files.newBufferedReader(file));
				return loader.build();
			}
			else {
				return NameStore.EMPTY;
			}
		}

		/**
		 * Loads contents of an area.
		 * @param xml XML
		 * @param ctx Context
		 */
		private void loadAreaContents(Element xml, LoaderContext ctx) {
			switch(xml.name()) {
			case "location":
				// Load world location
				final DefaultLocation connector = loaders.location.load(xml, ctx);
				ctx.add(connector);
				LOG.info("Loaded world location: " + connector.name());
				break;

			case "grid":
				// Load grid
				final Grid grid = loaders.grid.load(xml, ctx);
				LOG.info("Loaded grid: " + grid);
				break;

			case "path":
				// Load path
				loaders.path.load(xml, ctx);
				LOG.info("Loaded path");
				break;

			case "resource":
			case "weather":
			case "faction":
			case "ambient":
				// Ignore area elements
				// TODO - nasty
				break;

			default:
				// Invalid element
				throw xml.exception("Invalid area element type: " + xml.name());
			}
		}
	}
}
