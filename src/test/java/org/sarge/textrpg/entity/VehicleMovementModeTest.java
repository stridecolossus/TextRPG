package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.Vehicle.AbstractVehicle;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Trail;

public class VehicleMovementModeTest {
	private VehicleMovementMode mode;
	private AbstractVehicle vehicle;

	@BeforeEach
	public void before() {
		vehicle = mock(AbstractVehicle.class);
		when(vehicle.name()).thenReturn("vehicle");
		when(vehicle.trail()).thenReturn(new Trail());
		when(vehicle.noise()).thenReturn(Percentile.HALF);
		mode = new VehicleMovementMode(vehicle);
	}

	@Test
	public void constructor() {
		assertEquals(vehicle, mode.mover());
		assertEquals(Percentile.HALF, mode.noise());
		assertNotNull(mode.trail());
	}
}
