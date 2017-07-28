package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Test;
import org.sarge.textrpg.common.Clock.Listener;

public class ClockTest {
	@After
	public void after() {
		Clock.CLOCK.reset();
	}
	
	@Test
	public void constructor() {
		assertEquals(Duration.ofMinutes(5).toMillis(), Clock.HOUR);
	}
	
	@Test
	public void next() {
		final LocalDateTime datetime = Clock.CLOCK.getWorldTime();
		Clock.CLOCK.next();
		assertEquals(datetime.plusHours(1), Clock.CLOCK.getWorldTime());
	}
	
	@Test
	public void listener() {
		final Listener listener = mock(Listener.class);
		Clock.CLOCK.add(listener);
		Clock.CLOCK.next();
		verify(listener).update(Clock.CLOCK.getHour());
	}
	
	@Test
	public void remove() {
		final Listener listener = mock(Listener.class);
		Clock.CLOCK.add(listener);
		Clock.CLOCK.remove(listener);
		Clock.CLOCK.next();
		verifyZeroInteractions(listener);
	}
}
