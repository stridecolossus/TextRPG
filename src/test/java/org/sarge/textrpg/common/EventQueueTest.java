package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EventQueueTest {
	private EventQueue queue;
	private Runnable event;

	@Before
	public void before() {
		queue = new EventQueue();
		event = mock(Runnable.class);
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
		assertEquals(1, queue.size());
		assertEquals(entry, queue.stream().iterator().next());
	}

	@Test
	public void cancel() {
		final EventQueue.Entry entry = queue.add(event, 1L);
		entry.cancel();
		assertEquals(true, entry.isCancelled());
		EventQueue.update(1);
		assertEquals(0, queue.stream().count());
		verifyZeroInteractions(event);
	}

	@Test
	public void update() {
		final EventQueue.Entry other = queue.add(mock(Runnable.class), 2L);
		final EventQueue.Entry entry = queue.add(event, 1L);
		EventQueue.update(1L);
		verify(event).run();
		assertEquals(1, queue.size());
		assertEquals(other, queue.stream().iterator().next());
		assertEquals(true, entry.isCancelled());
	}

	@Ignore("No longer checked")
	@Test(expected = RuntimeException.class)
	public void executeInvalidTime() {
		EventQueue.update(-1L);
	}

	@Test
	public void repeating() {
		queue.add(event, 1L, true);
		EventQueue.update(1L);
		verify(event).run();
		assertEquals(1, queue.size());
	}

	@Test
	public void clear() {
		queue.add(event, 1L);
		queue.reset();
		assertEquals(0, queue.size());
	}
}
