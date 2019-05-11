package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.Element.ElementException;

public class PathLoaderTest {
	private PathLoader loader;
	private LocationLoaderHelper helper;
	private Location.Linker linker;

	@BeforeEach
	public void before() {
		// Create location loader helper
		helper = mock(LocationLoaderHelper.class);
		when(helper.loadDescriptor(any(Element.class), any())).thenReturn(new Location.Descriptor("loc"));

		// Create linker
		linker = mock(Location.Linker.class);

		// Create loader
		loader = new PathLoader(helper, linker);
	}

	/**
	 * Creates a path location XML fragment.
	 */
	private static Element create(String dir) {
		return new Element.Builder("location")
			.attribute("name", "loc")
			.attribute("dir", dir)
			.build();
	}

	@Test
	public void load() {
		// Build path XML (note first direction is ignored)
		final Element xml = new Element.Builder("path")
			.attribute("route", "tunnel")
			.add(create("d"))
			.add(create("n"))
			.add(create("e"))
			.build();

		// Init context
		final LoaderContext ctx = mock(LoaderContext.class);
		when(ctx.area()).thenReturn(Area.ROOT);

		// Load path
		final List<Location> connectors = loader.load(xml, ctx);
		assertNotNull(connectors);
		assertEquals(2, connectors.size());

		// Check start location
		final Location start = connectors.get(0);
		assertNotNull(start);
		assertEquals("loc", start.name());
		assertEquals(1, start.exits().stream().count());
		assertTrue(start.exits().find(Direction.NORTH).isPresent());

		// Check middle
		final Location middle = start.exits().find(Direction.NORTH).get().destination();
		assertEquals("loc", middle.name());
		assertEquals(2, middle.exits().stream().count());
		assertEquals(Optional.of(new Exit(Direction.SOUTH, RouteLink.of(Route.TUNNEL), start)), middle.exits().find(Direction.SOUTH));
		assertTrue(middle.exits().find(Direction.EAST).isPresent());

		// Check end
		final Location end = middle.exits().find(Direction.EAST).get().destination();
		assertEquals("loc", end.name());
		assertEquals(1, end.exits().stream().count());
		assertEquals(Optional.of(new Exit(Direction.WEST, RouteLink.of(Route.TUNNEL), middle)), end.exits().find(Direction.WEST));
	}

	@Test
	public void loadEmptyPath() {
		assertThrows(ElementException.class, () -> loader.load(Element.of("path"), mock(LoaderContext.class)));
	}
}
