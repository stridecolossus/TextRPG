package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.Description;

public class WeatherActionTest extends ActionTestBase {
	private WeatherAction action;

	@BeforeEach
	public void before() {
		action = new WeatherAction();
	}

	@Test
	public void weather() {
		final Weather weather = mock(Weather.class);
		final Area area = new Area.Builder("area").weather(weather).build();
		when(weather.describe()).thenReturn(Description.of("weather"));
		when(loc.area()).thenReturn(area);
		action.weather(actor);
		verify(weather).describe();
	}

	@Test
	public void weatherNone() {
		final Weather weather = mock(Weather.class);
		final Area area = new Area.Builder("area").weather(Weather.NONE).build();
		when(weather.describe()).thenReturn(Description.of("weather"));
		when(loc.area()).thenReturn(area);
		assertEquals(Response.of("weather.none"), action.weather(actor));
	}
}
