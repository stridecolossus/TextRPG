package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Percentile;

public class TimePeriodTest {
	private TimePeriod period;

	@BeforeEach
	public void before() {
		period = new TimePeriod("dusk", LocalTime.of(19, 0), Percentile.HALF);
	}

	@Test
	public void constructor() {
		assertEquals("dusk", period.name());
		assertEquals(LocalTime.of(19, 0), period.start());
		assertEquals(Percentile.HALF, period.light());
	}

	@Test
	public void load() throws IOException {
		final List<TimePeriod> periods = TimePeriod.load(new StringReader("one 08:00 50 \n two 09:00 100"));
		final TimePeriod one = new TimePeriod("one", LocalTime.of(8, 0), Percentile.HALF);
		final TimePeriod two = new TimePeriod("two", LocalTime.of(9, 0), Percentile.ONE);
		assertEquals(List.of(one, two), periods);
	}
}
