package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.world.WeatherController.Listener;

public class WeatherControllerTest {
	private WeatherController controller;
	private Event.Queue queue;

	@BeforeEach
	public void before() {
		queue = new Event.Queue.Manager().queue("weather");
		controller = new WeatherController(42, Duration.ofHours(1), queue);
	}

	@Test
	public void constructor() {
		assertEquals(1, queue.size());
	}

	@Test
	public void randomise() {
		final Weather weather = mock(Weather.class);
		controller.add(weather);
		controller.randomise();
		verify(weather).randomise(42);
	}

	@Test
	public void listener() {
		final Listener listener = mock(Listener.class);
		controller.add(listener);
		controller.randomise();
		verify(listener).update();
	}
}
