package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.util.Event;

public class AmbientControllerTest extends ActionTestBase {
	private AmbientController controller;
	private Event.Queue queue;

	@BeforeEach
	public void before() {
		final Event.Queue.Manager manager = new Event.Queue.Manager();
		queue = manager.queue("ambient");
		controller = new AmbientController(queue);
	}

	@Test
	public void updateSameArea() {
		final Exit exit = new Exit(Direction.EAST, Link.DEFAULT, loc);
		controller.update(actor, exit, loc);
		assertEquals(0, queue.size());
	}

	@Test
	public void update() {
		// Create area with an ambient event
		final AmbientEvent ambient = new AmbientEvent("name", DURATION, false);
		final Area area = new Area.Builder("other").ambient(ambient).build();
		when(loc.area()).thenReturn(area);

		final Location prev = mock(Location.class);
		when(loc.isTransition(prev)).thenReturn(true);

		// Notify move to area
		final Exit exit = new Exit(Direction.EAST, Link.DEFAULT, loc);
		controller.update(actor, exit, prev);

		// Check ambient event generated
		assertEquals(1, queue.size());

		// Invoke ambient event
		queue.manager().advance(DURATION.toMillis());
		verify(actor).alert(ambient.description());
	}
}
