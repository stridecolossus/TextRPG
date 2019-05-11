package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;

public class DefaultVehicleTest {
	private DefaultVehicle vehicle;

	@BeforeEach
	public void before() {
		vehicle = new DefaultVehicle(new DefaultVehicle.Descriptor(ObjectDescriptor.fixture("vehicle"), LimitsMap.EMPTY, Set.of(Terrain.GRASSLAND), Set.of(Route.ROAD), Percentile.HALF));
	}

	@Test
	public void constructor() {
		assertEquals("vehicle", vehicle.name());
		assertEquals(false, vehicle.isRaft());
		assertEquals(Percentile.HALF, vehicle.noise());
		assertEquals(Percentile.ONE, vehicle.tracks());
	}

	@Test
	public void isValid() {
		assertEquals(true, vehicle.isValid(exit(Terrain.GRASSLAND, Route.ROAD)));
		assertEquals(false, vehicle.isValid(exit(Terrain.GRASSLAND, Route.BRIDGE)));
		assertEquals(false, vehicle.isValid(exit(Terrain.DESERT, Route.ROAD)));
	}

	private static Exit exit(Terrain terrain, Route route) {
		// Create destination
		final Location dest = mock(Location.class);
		when(dest.terrain()).thenReturn(terrain);

		// Create link
		final Link link = mock(Link.class);
		when(link.route()).thenReturn(route);

		// Create exit
		return new Exit(Direction.EAST, link, dest);
	}
}
