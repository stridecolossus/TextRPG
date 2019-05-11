package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.Weather.Component;

public class WeatherTest {
	private Weather weather;

	@BeforeEach
	public void before() {
		weather = new Weather.Builder()
			.min(Component.PRECIPITATION, 1)
			.min(Component.WIND, 1)
			.max(Component.WIND, 3)
			.build();
	}

	@Test
	public void constructor() {
		assertNotNull(weather);
		assertEquals(1, weather.snow());
		assertNotNull(weather.history());
		assertEquals(1, weather.history().count());
	}

	@Test
	public void current() {
		assertEquals(1, weather.snow());
		assertEquals(true, weather.isFrozen());
		final Weather.Entry current = weather.history().iterator().next();
		assertEquals(1, current.get(Component.PRECIPITATION));
		assertEquals(0, current.get(Component.TEMPERATURE));
		assertEquals(1, current.get(Component.WIND));
	}

	@Test
	public void invalidWeatherLevel() {
		assertThrows(IllegalArgumentException.class, () -> new Weather.Builder().max(Component.PRECIPITATION, 5).build());
	}

	@Test
	public void invalidMinMaxWeatherLevel() {
		assertThrows(IllegalArgumentException.class, () -> new Weather.Builder().min(Component.PRECIPITATION, 2).max(Component.PRECIPITATION, 1).build());
	}

	@Test
	public void describe() {
		final Description expected = new Description.Builder("weather.description").add("weather", "frozen.light.breezy").add("snow", 1).build();
		assertEquals(expected, weather.describe());
	}

	@Test
	public void difference() {
		final Description diff = weather.difference();
		// TODO
	}

	@RepeatedTest(10)
	public void randomise() {
		weather.randomise(2);
		final Weather.Entry current = weather.history().iterator().next();
		assertEquals(1, current.get(Component.PRECIPITATION));
		assertEquals(0, current.get(Component.TEMPERATURE));
		assertTrue(current.get(Component.WIND) >= 1);
		assertTrue(current.get(Component.WIND) < 3);
		assertEquals(true, weather.isFrozen());
		assertEquals(2, weather.snow());
		assertEquals(2, weather.history().count());
	}

	@RepeatedTest(10)
	public void randomiseHistoryLimit() {
		weather.randomise(2);
		assertEquals(2, weather.history().count());
	}

	@Test
	public void none() {
		assertEquals(false, Weather.NONE.isFrozen());
	}

	@Test
	public void randomiseWeatherNone() {
		assertThrows(UnsupportedOperationException.class, () -> Weather.NONE.randomise(42));
	}
}
