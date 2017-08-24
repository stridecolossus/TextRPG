package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Clock.Listener;

public class ClockTest {
	private static final long HOUR = 10;
	private Clock clock;
	
	@Before
	public void before() {
		clock = new Clock(HOUR);
	}
	
	@Test
	public void constructor() {
		assertEquals(0, clock.getHour());
	}
	
	@Test
	public void update() {
		// Check update within current hour
		clock.update(1);
		assertEquals(0, clock.getHour());
		
		// Check next hour
		clock.update(10);
		assertEquals(1, clock.getHour());

		// Check last hour-of-day
		clock.update(10 * 23);
		assertEquals(23, clock.getHour());

		// Check cycles to start-of-day
		clock.update(10 * 24);
		assertEquals(0, clock.getHour());
	}
	
	@Test
	public void listener() {
		// Add a listener
		final Listener listener = mock(Listener.class);
		clock.add(listener);
		
		// Check no updates within current hour
		clock.update(1);
		verifyZeroInteractions(listener);
		
		// Check update for a new hour
		clock.update(25);
		verify(listener).update(2);
	}
}
