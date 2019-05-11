package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FacilityRegistryTest {
	private FacilityRegistry registry;
	private Location loc;

	@BeforeEach
	public void before() {
		registry = new FacilityRegistry();
		loc = mock(Location.class);
	}

	@Test
	public void find() {
		final String facility = "facility";
		registry.add(loc, facility);
		assertEquals(Optional.of(facility), registry.find(loc, String.class));
	}

	@Test
	public void findNotPresent() {
		assertEquals(Optional.empty(), registry.find(loc, Integer.class));
	}

	@Test
	public void addOccupied() {
		final String facility = "facility";
		registry.add(loc, facility);
		assertThrows(IllegalArgumentException.class, () -> registry.add(loc, facility));
	}
}
