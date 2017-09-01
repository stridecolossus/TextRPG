package org.sarge.textrpg.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;

public class EventHolderTest {
	private EventHolder holder;

	@Before
	public void before() {
		holder = new EventHolder();
	}

	@Test
	public void set() {
		final EventQueue.Entry prev = mock(EventQueue.Entry.class);
		final EventQueue.Entry next = mock(EventQueue.Entry.class);
		holder.set(prev);
		holder.set(next);
		verify(prev).cancel();
		verifyZeroInteractions(next);
	}

	@Test
	public void cancel() {
		final EventQueue.Entry entry = mock(EventQueue.Entry.class);
		holder.set(entry);
		holder.cancel();
		verify(entry).cancel();
	}
}
