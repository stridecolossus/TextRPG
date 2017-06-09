package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;

public class EventQueueTest {
	private EventQueue queue;
	private Event event;
	
	@Before
	public void before() {
		queue = new EventQueue();
		event = mock(Event.class);
	}
	
	@Test
	public void constructor() {
		assertEquals(0, queue.stream().count());
	}
	
	@Test
	public void add() {
		final EventQueue.Entry entry = queue.add(event, 1L);
		assertNotNull(entry);
		assertEquals(false, entry.isCancelled());
		assertEquals(1, queue.stream().count());
		assertEquals(entry, queue.stream().iterator().next());
	}

	@Test
	public void cancel() {
		final EventQueue.Entry entry = queue.add(event, 1L);
		entry.cancel();
		assertEquals(true, entry.isCancelled());
		queue.update(1);
		assertEquals(0, queue.stream().count());
		verifyZeroInteractions(event);
	}
	
	@Test
	public void update() {
		final EventQueue.Entry other = queue.add(mock(Event.class), 2L);
		final EventQueue.Entry entry = queue.add(event, 1L);
		queue.update(1L);
		verify(event).execute();
		assertEquals(1, queue.stream().count());
		assertEquals(other, queue.stream().iterator().next());
		assertEquals(true, entry.isCancelled());
	}
	
	@Test(expected = RuntimeException.class)
	public void executeInvalidTime() {
		queue.update(-1L);
	}
	
	@Test
	public void repeating() {
		queue.add(event, 1L, true);
		queue.update(1L);
		verify(event).execute();
		assertEquals(1, queue.stream().count());
	}
	
	@Test
	public void clear() {
		queue.add(event, 1L);
		queue.reset();
		assertEquals(0, queue.stream().count());
	}
}
