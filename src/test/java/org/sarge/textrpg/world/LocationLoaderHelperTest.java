package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;

public class LocationLoaderHelperTest {
	private LocationLoaderHelper helper;
	private LinkLoader loader;
	private Location.Linker linker;

	@BeforeEach
	public void before() {
		loader = mock(LinkLoader.class);
		linker = mock(Location.Linker.class);
		helper = new LocationLoaderHelper(loader, linker);
	}

	@Test
	public void loadDescriptor() {
		// Create location descriptor XML
		final Element xml = new Element.Builder("loc")
			.attribute("name", "loc")
			.attribute("terrain", "farmland")
			.child("property")
				.text("fish")
				.end()
			.build();

		// Build expected result
		final var expected = new Location.Descriptor.Builder()
			.name("loc")
			.terrain(Terrain.FARMLAND)
			.property(Property.FISH)
			.build();

		// Load descriptor
		final Location.Descriptor descriptor = helper.loadDescriptor(xml, Terrain.DESERT);
		assertEquals(expected, descriptor);
	}

	@Test
	public void loadExit() {
		// Create exit descriptor
		final Element xml = new Element.Builder("link")
			.attribute("dir", "east")
			.attribute("dest", "dest")
			.attribute("reverse", "inverse")
			.attribute("reverse-dir", "down")
			.build();

		// Load exit
		final LoaderContext ctx = mock(LoaderContext.class);
		final var connector = mock(Location.class);
		when(loader.load(xml, connector, ctx)).thenReturn(Link.DEFAULT);
		helper.loadExit(xml, connector, ctx);

		// Check added to linker
		final LinkedExit exit = new LinkedExit(connector, Direction.EAST, Link.DEFAULT, "dest", LinkedExit.ReversePolicy.INVERSE, Direction.DOWN);
		verify(linker).add(exit);
	}
}
