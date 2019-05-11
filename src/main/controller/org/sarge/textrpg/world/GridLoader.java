package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toMap;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.sarge.lib.collection.Pair;
import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.Matrix;
import org.sarge.textrpg.util.Matrix.Coordinates;
import org.sarge.textrpg.world.Grid.Builder.Cursor;
import org.springframework.stereotype.Service;

/**
 * Loader for a grid.
 * <p>
 * Grid XML format:
 * <p>
 * <table border=1>
 * <tr>
 *   <th>name</th>
 *   <th>XML type</th>
 *   <th>number</th>
 *   <th>optional</th>
 *   <th>purpose</th>
 * </tr>
 * <tr>
 *   <td>default</td>
 *   <td>element</td>
 *   <td>one</td>
 *   <td>yes</td>
 *   <td>default location descriptor</td>
 * </tr>
 * <tr>
 *   <td>descriptor</td>
 *   <td>element</td>
 *   <td>many</td>
 *   <td>yes</td>
 *   <td>maps a grid icon to a location descriptor</td>
 * </tr>
 * <tr>
 *   <td>empty</td>
 *   <td>attribute</td>
 *   <td>one</td>
 *   <td>yes</td>
 *   <td>empty location icon (default is <b>X</b>)</td>
 * </tr>
 * <tr>
 *   <td>map</td>
 *   <td>text</td>
 *   <td>one</td>
 *   <td>no</td>
 *   <td>terrain map</td>
 * </tr>
 * <tr>
 *   <td>neighbour</td>
 *   <td>element</td>
 *   <td>cardinal directions</td>
 *   <td>yes</td>
 *   <td>attaches neighbouring grids</td>
 * </tr>
 * <tr>
 *   <td>build</td>
 *   <td>element</td>
 *   <td>one</td>
 *   <td>yes</td>
 *   <td>grid operations for a given cursor (as follows)</td>
 * </tr>
 * <tr>
 *   <td>block</td>
 *   <td>element</td>
 *   <td>one</td>
 *   <td>no</td>
 *   <td>blocks an exit in a given direction</td>
 * </tr>
 * <tr>
 *   <td>expose</td>
 *   <td>element</td>
 *   <td>one</td>
 *   <td>no</td>
 *   <td>exposes a grid location as a <i>connector</i></td>
 * </tr>
 * <tr>
 *   <td>route</td>
 *   <td>element</td>
 *   <td>one</td>
 *   <td>no</td>
 *   <td>creates a route in the grid from a given direction path</td>
 * </tr>
 * </table>
 * </p>
 * @author Sarge
 */
@Service
public class GridLoader {
	/**
	 * Maps icons to terrain.
	 */
	private static final Map<Character, Terrain> TERRAIN = Arrays.stream(Terrain.values()).collect(toMap(Terrain::icon, Function.identity()));

	/**
	 * Converter for a location descriptor mapping icon.
	 */
	private static final Converter<Character> ICON_CONVERTER = str -> {
		if(str.length() != 1) throw new IllegalArgumentException(String.format("Invalid icon length: [%s]", str));
		return str.charAt(0);
	};

	private final LocationLoaderHelper helper;
	private final Location.Linker linker;

	/**
	 * Constructor.
	 * @param helper		Location loader helper
	 * @param linker		Location linker
	 */
	public GridLoader(LocationLoaderHelper helper, Location.Linker linker) {
		this.helper = notNull(helper);
		this.linker = notNull(linker);
	}

	/**
	 * Loads a location grid.
	 * @param xml		XML
	 * @param area		Parent area
	 * @return Grid
	 * @throws IllegalArgumentException if the grid is empty or invalid
	 */
	public Grid load(Element xml, LoaderContext ctx) {
		// Load location descriptors
		final Matrix<Location.Descriptor> descriptors = loadDescriptors(xml);
		// TODO - load mappings/default/empty here so can re-use to lookup cursors?

		// Create builder
		final Grid.Builder builder = new Grid.Builder(ctx.area(), descriptors);

		// Apply grid operations
		xml.find("build").stream().flatMap(Element::children).forEach(e -> load(e, builder, ctx));

		// Attach neighbours
		xml.children("neighbour").forEach(e -> neighbour(e, ctx, builder));

		// Construct grid
		final Grid grid = builder.build();

		// Register grid
		xml.attribute("ref").optional().ifPresent(ref -> ctx.add(ref, grid));

		return grid;
	}

	/**
	 * Loads the terrain-descriptor grid.
	 * @param xml XML
	 * @return Location descriptors
	 */
	private Matrix<Location.Descriptor> loadDescriptors(Element xml) {
		// Load terrain-descriptor mappings
		final Location.Descriptor def = helper.loadDescriptor(xml.child("default"), Terrain.DARK);
		final var mappings = xml.children("descriptor").map(this::mapping).collect(toMap(Pair::left, Pair::right));

		// Load empty icon
		final char empty = loadEmptyIcon(xml, mappings.keySet());

		// Load terrain grid
		final CharacterGridLoader loader = new CharacterGridLoader();
		final String text = xml.child("map").text();
		final Matrix<Character> grid = loader.load(text);

		// Create icon mapper
		final Function<Character, Location.Descriptor> mapper = ch -> {
			// Check for empty location
			if(ch.charValue() == empty) {
				return null;
			}

			// Lookup or load location descriptor
			final Location.Descriptor descriptor = mappings.get(ch);
			if(descriptor == null) {
				// Load custom descriptor
				final Terrain terrain = TERRAIN.get(ch);
				if(terrain == null) throw new IllegalArgumentException("Invalid terrain icon: " + ch);
				final Location.Descriptor custom = new Location.Descriptor(def, terrain);
				mappings.put(ch, custom);
				return custom;
			}
			else {
				// Use pre-defined descriptor
				return descriptor;
			}
		};

		// Convert terrain to location descriptors
		return grid.map(mapper);
	}

	/**
	 * Loads the empty location icon.
	 */
	private static char loadEmptyIcon(Element xml, Set<Character> mappings) {
		final Element.Attribute attr = xml.attribute("empty");
		if(attr.isPresent()) {
			// Load and validate empty icon string
			final String str = attr.toText();
			if(str.length() != 1) throw xml.exception("Invalid empty icon: " + str);

			// Check empty icon does not duplicate terrain or mapping icons
			final char empty = str.charAt(0);
			if(TERRAIN.keySet().contains(empty)) throw new IllegalArgumentException("Empty icon duplicates terrain icon: " + empty);
			if(mappings.contains(empty)) throw new IllegalArgumentException("Empty icon duplicates mapping icon: " + empty);
			return empty;
		}
		else {
			return 'X';
		}
	}

	/**
	 * Loads a location descriptor mapping for either a POI or terrain icon.
	 * @param xml XML
	 * @return Mapping
	 */
	private Pair<Character, Location.Descriptor> mapping(Element xml) {
		final Location.Descriptor descriptor = helper.loadDescriptor(xml);
		final Character icon = xml.attribute("icon").toValue(descriptor.terrain().icon(), ICON_CONVERTER);
		return Pair.of(icon, descriptor);
	}

	/**
	 * Loads an applies grid operations.
	 * @param xml			XML
	 * @param builder		Grid builder
	 */
	private void load(Element xml, Grid.Builder builder, LoaderContext ctx) {
		// Load cursor
		final Coordinates coords = coordinates(xml);
		final Cursor cursor = builder.cursor(coords.x, coords.y);

		// Apply operation
		switch(xml.name()) {
		case "block":
			// Block a default exit
			final Direction dir = xml.attribute("dir").toValue(Direction.CONVERTER);
			final boolean bi = xml.attribute("bi-directional").toBoolean(true);
			cursor.block(dir, bi);
			break;

		case "expose":
			// Expose a location as a connector
			final Location connector = cursor.connector();
			linker.add(connector);
			break;

		case "route":
			// Add a route
			final Route route = xml.attribute("route").toValue(ctx.route(), Route.CONVERTER);
			final String path = xml.attribute("path").toText();
			cursor.route(route, Direction.path(path));
			break;

		default:
			throw xml.exception("Invalid grid operation: " + xml.name());
		}
	}

	/**
	 * Helper - Loads grid coordinates.
	 */
	private static Coordinates coordinates(Element xml) {
		// Parse cursor coordinates
		final String[] parts = xml.attribute("cursor").toText().trim().split(",");
		if(parts.length != 2) throw xml.exception("Expected x,y cursor");

		// Convert to coordinates
		final int x = Integer.parseInt(parts[0].trim());
		final int y = Integer.parseInt(parts[1].trim());

		// Create cursor
		return new Coordinates(x, y);
	}

	/**
	 * Loads and attaches a neighbouring grid.
	 * @param xml 			XML
	 * @param ctx			Context
	 * @param builder		Grid builder
	 */
	private static void neighbour(Element xml, LoaderContext ctx, Grid.Builder builder) {
		// Load neighbour details
		final String name = xml.attribute("name").toText();
		final int offset = xml.attribute("offset").toInteger();
		final Direction side = xml.attribute("side").toValue(Direction.CONVERTER);

		// Lookup neighbouring grid
		final Grid neighbour = ctx.grid(name);
		if(neighbour == null) throw xml.exception("Unknown neighbouring grid: " + name);

		// Attach neighbour
		builder.neighbour(neighbour, side, offset);
	}
}
