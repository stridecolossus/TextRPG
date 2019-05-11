package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.Matrix;
import org.sarge.textrpg.util.Matrix.Coordinates;

public class GridLoaderTest {
	private GridLoader loader;
	private Location.Linker linker;
	private LocationLoaderHelper helper;

	@BeforeEach
	public void before() {
		linker = mock(Location.Linker.class);
		helper = new LocationLoaderHelper(mock(LinkLoader.class), linker);
		loader = new GridLoader(helper, linker);
	}

	@Test
	public void load() {
		// Build grid XML
		final Element xml = new Element.Builder("grid")
			// Default descriptor
			.child("default")
				.attribute("name", "default")
				.end()
			// Terrain mapper
			.child("descriptor")
				.attribute("name", "grass")
				.attribute("terrain", "grassland")
				.end()
			// POI
			.child("descriptor")
				.attribute("icon", "A")
				.attribute("name", "POI")
				.attribute("terrain", "farmland")
				.child("property")
					.text("fish")
					.end()
				.end()
			// Grid
			.child("map")
				.text("-AX\n---")
				.end()
			// Neighbours
			.child("neighbour")
				.attribute("name", "ref")
				.attribute("side", Direction.EAST)
				.attribute("offset", 0)
				.end()
			// Operations
			.child("build")
				// Blocked exit
				.child("block")
					.attribute("cursor", "0,0")
					.attribute("dir", "e")
					.end()
				// Add a route
				.child("route")
					.attribute("cursor", "0,0")
					.attribute("path", "sen")
					.attribute("route", "lane")
					.end()
				// Expose a connector
				.child("expose")
					.attribute("cursor", "0,0")
					.end()
				.end()
			.build();

		// Init context
		final LoaderContext ctx = mock(LoaderContext.class);
		when(ctx.area()).thenReturn(Area.ROOT);

		// Create neighbour
		final Grid neighbour = new Grid.Builder(Area.ROOT, new Matrix<Location.Descriptor>(2, 3)).build();
		when(ctx.grid("ref")).thenReturn(neighbour);

		// Load grid
		final Grid grid = loader.load(xml, ctx);
		assertNotNull(grid);

		// Check default location
		check(grid, 0, 0, new Location.Descriptor.Builder().name("grass").terrain(Terrain.GRASSLAND).build());

		// Check POI
		check(grid, 1, 0, new Location.Descriptor.Builder().name("POI").terrain(Terrain.FARMLAND).property(Property.FISH).build());

		// Check empty location
		assertEquals(null, grid.get(new Coordinates(2, 0)));

		// Check blocked exit
		assertEquals(Optional.empty(), grid.get(new Coordinates(0, 0)).exits().find(Direction.EAST));

		// Check exposed location
		final ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
		verify(linker).add(captor.capture());
		final Location connector = captor.getValue();
		assertNotNull(connector);
		assertEquals("grass", connector.name());

		// Check route
		route(grid, 0, 0, Direction.SOUTH);
		route(grid, 0, 1, Direction.EAST);
		route(grid, 1, 1, Direction.NORTH);
	}

	private static void check(Grid grid, int x, int y, Location.Descriptor expected) {
		final Location loc = grid.get(new Coordinates(x, y));
		if(expected == null) {
			assertEquals(null, loc);
		}
		else {
			assertEquals(expected, loc.descriptor());
		}
	}

	private static void route(Grid grid, int x, int y, Direction dir) {
		assertEquals(Route.LANE, grid.get(new Coordinates(x, y)).exits().find(dir).get().link().route());
	}
}
