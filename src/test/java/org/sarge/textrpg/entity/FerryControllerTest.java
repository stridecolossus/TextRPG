package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.collection.LoopIterator;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.entity.Ferry.Ticket;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;

public class FerryControllerTest extends ActionTestBase {
	private FerryController controller;
	private Event.Queue queue;
	private Ferry ferry;

	@BeforeEach
	public void before() {
		queue = new Event.Queue.Manager().queue("ferry");
		controller = new FerryController(queue);
		ferry = mock(Ferry.class);
	}

	@Test
	public void start() {
		// Init iterator
		when(ferry.iterator(LoopIterator.Strategy.CYCLE)).thenReturn(new LoopIterator<>(List.of(loc)));

		// Start controller
		controller.start(ferry, LoopIterator.Strategy.CYCLE, Duration.ofMinutes(1));
		assertEquals(1, queue.size());

		// Check ferry moved
		queue.manager().advance(Duration.ofMinutes(1).toMillis());
		verify(ferry).move(loc);
	}

	@Test
	public void disembark() {
		// Create ticket
		final Ticket ticket = mock(Ticket.class);
		when(ticket.ferry()).thenReturn(ferry);
		when(ticket.destination()).thenReturn(loc);
		when(ferry.isTicketRequired()).thenReturn(true);
		when(ferry.name()).thenReturn("ferry");

		// Register ticket to destination
		controller.register(actor, ticket);
		when(actor.parent()).thenReturn(ferry);
		when(ferry.parent()).thenReturn(loc);

		// Start ferry
		start();

		// Check actor disembarked
		verify(actor).parent(loc);
		verify(actor).alert(new Description("ferry.disembark", "ferry"));
	}
}
