package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EventTest {
	private static final Duration DURATION = Duration.ofMillis(1);

	private Event.Queue.Manager manager;
	private Event.Queue queue;
	private Event event;

	@BeforeEach
	public void before() {
		manager = new Event.Queue.Manager();
		queue = manager.queue("queue", true);
		event = mock(Event.class);
	}

	@Test
	public void constructor() {
		assertNotNull(queue);
		assertEquals("queue", queue.name());
		assertEquals(0, queue.size());
		assertEquals(manager, queue.manager());
	}

	@Test
	public void remove() {
		queue.add(event, DURATION);
		queue.remove();
		manager.advance(DURATION.toMillis());
		verifyZeroInteractions(event);
	}

	@Test
	public void removeNotTransient() {
		queue = manager.queue("permanent", false);
		assertThrows(IllegalStateException.class, () -> queue.remove());
	}

	@Test
	public void add() {
		final Event.Reference ref = queue.add(event, DURATION);
		assertNotNull(ref);
		assertEquals(false, ref.isCancelled());
		assertEquals(1, queue.size());
	}

	@DisplayName("Execute a scheduled event")
	@Test
	public void addExecute() {
		final Event.Reference ref = queue.add(event, DURATION);
		manager.advance(DURATION.toMillis());
		verify(event).execute();
		assertEquals(true, ref.isCancelled());
		assertEquals(0, queue.size());
	}

	@DisplayName("Executes multiple events and checks entry ordering")
	@Test
	public void addMultipleEvents() {
		// Add some events
		final Event other = mock(Event.class);
		queue.add(event, Duration.ofMillis(2));
		queue.add(other, Duration.ofMillis(1));

		// Update and check first event executed
		manager.advance(1);
		verify(other).execute();
		verifyZeroInteractions(event);
		assertEquals(1, queue.size());

		// Update and check second executed
		manager.advance(2);
		verify(event).execute();
		verifyNoMoreInteractions(other);
		assertEquals(0, queue.size());
	}

	@DisplayName("Execute a repeating event")
	@Test
	public void addRepeating() {
		when(event.execute()).thenReturn(true);
		queue.add(event, DURATION);
		manager.advance(DURATION.toMillis());
		verify(event).execute();
		assertEquals(1, queue.size());
	}

	@DisplayName("Cannot hold an event that has already been cancelled")
	@Test
	public void holderAlreadyCancelled() {
		final Event.Holder holder = new Event.Holder();
		final Event.Reference ref = queue.add(event, DURATION);
		ref.cancel();
		assertThrows(IllegalArgumentException.class, () -> holder.set(ref));
	}

	@DisplayName("Cancel a referenced event")
	@Test
	public void holderCancel() {
		final Event.Holder holder = new Event.Holder();
		holder.set(queue.add(event, DURATION));
		holder.cancel();
		manager.advance(DURATION.toMillis());
		verifyZeroInteractions(event);
	}

	@DisplayName("Replace a referenced event and cancel previous")
	@Test
	public void holderReplace() {
		final Event.Holder holder = new Event.Holder();
		holder.set(queue.add(event, DURATION));
		holder.set(queue.add(mock(Event.class), DURATION));
		manager.advance(DURATION.toMillis());
		verifyZeroInteractions(event);
	}

	@DisplayName("Cancel a referenced event that has already been cancelled")
	@Test
	public void holderCancelMultiple() {
		final Event.Holder holder = new Event.Holder();
		final Event.Reference ref = queue.add(event, DURATION);
		holder.set(ref);
		ref.cancel();
		holder.cancel();
	}
}
