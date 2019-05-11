package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Inventory;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Property;

public class ReceptacleControllerTest {
	private ReceptacleController controller;
	private Entity actor;
	private Location loc;
	private Receptacle rec;

	@BeforeEach
	public void before() {
		// Create location
		loc = mock(Location.class);
		when(loc.contents()).thenReturn(new Contents());

		// Create actor
		actor = mock(Entity.class);
		when(actor.location()).thenReturn(loc);
		when(actor.contents()).thenReturn(new Inventory());

		// Create water
		rec = new Receptacle(new Receptacle.Descriptor(ObjectDescriptor.of("water"), Liquid.WATER, 42));

		// Create controller
		controller = new ReceptacleController();
	}

	@Test
	public void findWaterGlobalSource() {
		when(loc.isProperty(Property.WATER)).thenReturn(true);
		assertEquals(Optional.of(ReceptacleController.GLOBAL_WATER), controller.findWater(actor));
	}

	@Test
	public void findWaterLocation() {
		rec.parent(loc);
		assertEquals(Optional.ofNullable(rec), controller.findWater(actor));
	}

	@Test
	public void findWaterInventory() {
		rec.parent(actor);
		assertEquals(Optional.ofNullable(rec), controller.findWater(actor));
	}

	@Test
	public void findWaterNone() {
		assertEquals(Optional.empty(), controller.findWater(actor));
	}
}
