package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

public class BoatTest {
	private Boat boat;
	private Location loc;
	private Exit exit;

	@BeforeEach
	public void before() {
		// Create boat
		boat = new Boat(new Boat.Descriptor(ObjectDescriptor.fixture("boat"), LimitsMap.EMPTY, true));
		boat.parent(TestHelper.parent());

		// Create location
		loc = mock(Location.class);
		when(loc.terrain()).thenReturn(Terrain.DESERT);
		when(loc.contents()).thenReturn(new Contents());

		// Create exit
		exit = Exit.of(Direction.EAST, loc);
	}

	@Test
	public void constructor() {
		assertEquals("boat", boat.name());
		assertEquals(true, boat.isRaft());
		assertEquals(false, boat.isMoored());
		assertEquals(Percentile.ZERO, boat.noise());
		assertEquals(Percentile.ZERO, boat.tracks());
	}

	private void water() {
		when(loc.isWater()).thenReturn(true);
	}

	@Test
	public void moveWater() {
		water();
		assertEquals(true, boat.isValid(exit));
		boat.move(loc);
		assertEquals(loc, boat.parent());
		assertEquals(false, boat.isMoored());
		assertEquals(true, boat.isValid(exit));
	}

	@Test
	public void moveLand() {
		when(loc.terrain()).thenReturn(Terrain.GRASSLAND);
		assertEquals(true, boat.isValid(exit));
		boat.move(loc);
		assertEquals(loc, boat.parent());
		assertEquals(true, boat.isMoored());
		assertEquals(false, boat.isValid(exit));
	}

	@Test
	public void moveFrozenWater() {
		water();
		when(loc.isFrozen()).thenReturn(true);
		assertEquals(true, boat.isValid(exit));
		boat.move(loc);
		assertEquals(loc, boat.parent());
		assertEquals(true, boat.isMoored());
		assertEquals(false, boat.isValid(exit));
	}
}
