package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Direction;

public class EmissionNotificationTest {
	private EmissionNotification notification;

	@BeforeEach
	public void before() {
		notification = new EmissionNotification(Emission.SMOKE, Percentile.ONE);
	}

	@Test
	public void constructor() {
		assertEquals("notification.emission.smoke", notification.key());
		assertEquals(Percentile.ONE, notification.intensity());
	}

	@Test
	public void describe() {
		final Description expected = new Description.Builder("notification.emission.smoke")
			.add("emission.intensity", "emission.intensity.high")
			.build();
		assertEquals(expected, notification.describe());
	}

	@Test
	public void scale() {
		final var scaled = notification.scale(Percentile.HALF, 1, Direction.EAST);
		final Description expected = new Description.Builder("notification.emission.smoke")
			.add("emission.intensity", "emission.intensity.medium")
			.add("dir", Direction.WEST)
			.build();
		assertEquals(expected, scaled.describe());
	}
}
