package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.OpeningTimes;
import org.sarge.textrpg.entity.LocationTriggerController;
import org.sarge.textrpg.util.PeriodModel;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.FacilityRegistry;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.LoaderContext;
import org.sarge.textrpg.world.Location;

public class ShopLoaderTest {
	private ShopLoader loader;
	private DefaultObjectDescriptorLoader descriptorLoader;
	private FacilityRegistry registry;
	private LocationTriggerController controller;

	@BeforeEach
	public void before() {
		descriptorLoader = mock(DefaultObjectDescriptorLoader.class);
		registry = new FacilityRegistry();
		controller = mock(LocationTriggerController.class);
		loader = new ShopLoader(descriptorLoader, registry, controller);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void load() {
		// Build XML
		final Element xml = new Element.Builder("xml")
			// Add shop attributes
			.attribute("name", "shop")
			// Add categories
			.child("cat")
				.text("stock-cat")
				.end()
			// Add stock
			.child("stock")
				.attribute("descriptor", "object")
				.attribute("num", 42)
				.end()
			// Add repair shop
			.child("repair")
				.child("cat")
					.text("repair-cat")
					.end()
				.end()
			.build();

		// Create descriptor
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("object").value(1).build();
		when(descriptorLoader.load(xml.child("stock"), ObjectDescriptorLoader.Policy.OBJECT)).thenReturn(descriptor);

		// Create faction for opening times
		final Faction faction = mock(Faction.class);
		when(faction.opening()).thenReturn(mock(PeriodModel.class));
		when(faction.opening().current()).thenReturn(new OpeningTimes(LocalTime.of(1, 2), true));

		// Init context
		final LoaderContext ctx = mock(LoaderContext.class);
		when(ctx.area()).thenReturn(Area.ROOT);
		when(ctx.faction()).thenReturn(Optional.of(faction));

		// Load shop
		final Location loc = mock(Location.class);
		final Shop shop = loader.load(xml, loc, ctx);
		assertNotNull(shop);
		assertEquals(Optional.of(shop), registry.find(loc, Shop.class));

		// Check stock
		assertEquals(1, shop.list(ObjectDescriptor.Filter.ALL).count());
		assertTrue(shop.stock(1).isPresent());
		assertEquals(new Shop.Stock(1, descriptor, 42), shop.stock(1).get());

		// Check repair shop
		assertTrue(shop.repair().isPresent());
	}
}
