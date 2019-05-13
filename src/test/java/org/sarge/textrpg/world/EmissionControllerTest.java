package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.entity.PerceptionCalculator;
import org.sarge.textrpg.util.Percentile;

public class EmissionControllerTest {
	private EmissionController controller;
	private Location start, other;
	private EmissionNotification light;

	@BeforeEach
	public void before() {
		// Create controller
		controller = new EmissionController(mock(PerceptionCalculator.class), 2, Percentile.HALF);

		// Create and link locations
		start = create();
		other = create();
		link(start, other);
		link(other, start);

		// Create emission
		light = new EmissionNotification(Emission.LIGHT, Percentile.ONE);
	}

	private static Location create() {
		final Location loc = mock(DefaultLocation.class);
		when(loc.terrain()).thenReturn(Terrain.DESERT);
		return loc;
	}

	private static void link(Location a, Location b) {
		final ExitMap exits = ExitMap.of(Exit.of(Direction.EAST, b));
		when(a.exits()).thenReturn(exits);
	}

	@Test
	public void find() {
		when(start.emission(Emission.LIGHT)).thenReturn(Percentile.ONE);
		when(other.emission(Emission.LIGHT)).thenReturn(Percentile.ONE);
		when(other.emission(Emission.SMOKE)).thenReturn(Percentile.HALF);
		final var expected = light.scale(Percentile.HALF, 1, Direction.EAST);
		assertNotNull(controller.find(Set.of(Emission.LIGHT), start));
		assertArrayEquals(new EmissionNotification[]{expected}, controller.find(Set.of(Emission.LIGHT), start).toArray());
	}
}
