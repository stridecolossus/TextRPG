package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectLoader;
import org.sarge.textrpg.object.ShopLoader;
import org.sarge.textrpg.object.WorldObject;

public class DefaultLocationLoaderTest {
	private DefaultLocationLoader loader;
	private LocationLoaderHelper helper;
	private Location.Linker linker;
	private ObjectLoader objectLoader;
	private ShopLoader shopLoader;

	@BeforeEach
	public void before() {
		helper = mock(LocationLoaderHelper.class);
		when(helper.loadDescriptor(any(), any())).thenReturn(new Location.Descriptor("loc"));
		linker = mock(Location.Linker.class);
		objectLoader = mock(ObjectLoader.class);
		shopLoader = mock(ShopLoader.class);
		loader = new DefaultLocationLoader(helper, linker, objectLoader, shopLoader);
	}

	@Test
	public void load() {
		// Create location XML
		final Element object = Element.of("object");
		final Element xml = new Element.Builder("xml")
			.attribute("orphan", true)
			.add("link")
			.child("links")
				.add("hidden")
				.end()
			.child("contents")
				.add(object)
				.end()
			.add("shop")
			.build();

		// Init context
		final LoaderContext ctx = mock(LoaderContext.class);
		when(ctx.area()).thenReturn(Area.ROOT);

		// Register contents
		final WorldObject obj = ObjectDescriptor.of("object").create();
		when(objectLoader.load(object, ctx)).thenReturn(obj);

		// Load location
		final Location loc = loader.load(xml, ctx);
		assertNotNull(loc);
		assertEquals("loc", loc.name());

		// Check location registered as a connector
		verify(linker).add(loc);

		// Check simple exit
		verify(helper).loadExit(xml.child("link"), loc, ctx);

		// Check custom exit
		verify(helper).loadExit(xml.child("links").child("hidden"), loc, ctx);

		// Check contents
		assertEquals(1, loc.contents().size());
		assertEquals(obj, loc.contents().stream().iterator().next());

		// Check shop loaded
		verify(shopLoader).load(xml.child("shop"), loc, ctx);
	}
}
