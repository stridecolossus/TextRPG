package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Clock.DateTimeClock;

public class DateTimeClockTest {
	private DateTimeClock adapter;
	private Clock clock;

	@BeforeEach
	public void before() {
		clock = mock(Clock.class);
		adapter = DateTimeClock.of(clock);
	}

	@Test
	public void now() {
		assertEquals(0, adapter.now());
	}

	@Test
	public void toDateTime() {
		final LocalDateTime date = adapter.toDateTime();
		assertNotNull(date);
		assertEquals(0, date.atZone(Clock.ZONE).toEpochSecond());
	}
}
