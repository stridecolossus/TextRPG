package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SnowModelTest {
	private SnowModel model;
	private Location loc;

	@BeforeEach
	public void before() {
		model = new SnowModel();
		loc = mock(Location.class);
	}

	/**
	 * Generates some snow.
	 */
	private void init() {
		final Weather weather = mock(Weather.class);
		final Area area = new Area.Builder("area").weather(weather).build();
		when(loc.area()).thenReturn(area);
		when(weather.snow()).thenReturn(3);
	}

	@DisplayName("Snow level delegates to area weather")
	@Test
	public void snow() {
		init();
		assertEquals(3, model.snow(loc));
	}

	@DisplayName("Snow level is zero when no weather")
	@Test
	public void snowNone() {
		when(loc.area()).thenReturn(Area.ROOT);
		assertEquals(0, model.snow(loc));
	}

	@DisplayName("Reducing snow level generates an entry for this location")
	@Test
	public void reduce() {
		init();
		model.reduce(loc, 1, 1);
		assertEquals(3 - 1, model.snow(loc));
	}

	@DisplayName("Snow level is clamped to zero")
	@Test
	public void reduceAlreadyZero() {
		init();
		model.reduce(loc, 999, 1);
		assertEquals(0, model.snow(loc));
	}

	@DisplayName("Expire a snow level modification")
	@Test
	public void update() {
		init();
		model.reduce(loc, 1, 1);
		model.update();
		assertEquals(3, model.snow(loc));
	}

	@DisplayName("Apply further snow to a modified entry")
	@Test
	public void updateWeather() {
		init();
		model.reduce(loc, 1, 999);
		when(loc.area().weather().snow()).thenReturn(4);
		model.update();
		assertEquals(4 - 1, model.snow(loc));
	}
}
