package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.BandingTable;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;

public class RepairControllerTest {
	private RepairController controller;
	private Event.Queue.Manager manager;

	@BeforeEach
	public void before() {
		final BandingTable<Percentile> table = new BandingTable.Builder<Percentile>().add(Percentile.ONE, "duration").build();
		manager = new Event.Queue.Manager();
		controller = new RepairController(manager);
		controller.setCost(3);
		controller.setDuration(Duration.ofMinutes(4));
		controller.setExpiry(Duration.ofMinutes(5));
		controller.setMapper(table);
	}

	@Test
	public void cost() {
		assertEquals(3, controller.cost());
	}

	@Test
	public void repair() {
		// Create a damaged object
		final DurableObject obj = mock(DurableObject.class);
		when(obj.wear()).thenReturn(2);

		// Start repair
		final Collection<WorldObject> pending = new ArrayList<>();
		controller.repair(obj, pending);

		// Advance time and check repaired object added to pending queue
		manager.advance(Duration.ofMinutes(2 * 4).toMillis());
		assertEquals(true, pending.contains(obj));
		verify(obj).repair();

		// Expire unclaimed object
		manager.advance(Duration.ofMinutes(2 * 4 + 5).toMillis());
		assertEquals(false, pending.contains(obj));
	}

	@Test
	public void description() {
		final DurableObject obj = mock(DurableObject.class);
		when(obj.condition()).thenReturn(Percentile.HALF);
		assertEquals("duration", controller.description(obj));
	}
}
