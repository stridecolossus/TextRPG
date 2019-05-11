package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.Entity.LocationTrigger;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Location;

public class LocationTriggerControllerTest {
	private LocationTriggerController controller;
	private Location loc;
	private LocationTrigger trigger;

	@BeforeEach
	public void before() {
		controller = new LocationTriggerController();
		trigger = mock(LocationTrigger.class);
		loc = mock(Location.class);
		when(loc.area()).thenReturn(Area.ROOT);
	}

	@Test
	public void add() {
		controller.add(loc, trigger);
		assertNotNull(controller.find(loc));
		assertArrayEquals(new LocationTrigger[]{trigger}, controller.find(loc).toArray());
	}

	@Test
	public void remove() {
		controller.add(loc, trigger);
		controller.remove(loc, trigger);
		assertEquals(0, controller.find(loc).count());
	}

	@Test
	public void update() {
		final Entity actor = mock(Entity.class);
		when(actor.location()).thenReturn(loc);
		controller.add(loc, trigger);
		controller.update(actor, null, null);
		verify(trigger).trigger(actor);
	}
}
