package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.ExitMap.MutableExitMap;

public class LocationTest {
	private Location loc;

	@BeforeEach
	public void before() {
		final var descriptor = new Location.Descriptor.Builder()
			.name("loc")
			.terrain(Terrain.WATER)
			.property(Property.FISH)
			.property(Property.BUSY)
			.build();

		loc = new Location(descriptor, new MutableExitMap()) {
			private final Area area = new Area.Builder("area").property(Property.FISH).build();

			@Override
			public Area area() {
				return area;
			}

			@Override
			public ExitMap exits() {
				return ExitMap.EMPTY;
			}
		};
	}

	@Test
	public void constructor() {
		// Check properties
		assertEquals("loc", loc.name());
		assertEquals(Terrain.WATER, loc.terrain());
		assertEquals(false, loc.isProperty(Property.FISH));
		assertEquals(true, loc.isProperty(Property.BUSY));
		assertEquals(true, loc.isWater());
		assertEquals(false, loc.isFrozen());

		// Check contents
		assertNotNull(loc.contents());
		assertNotNull(loc.tracks());

		// Check emissions
		for(Emission emission : Emission.values()) {
			assertEquals(Percentile.ZERO, loc.emission(emission));
		}
	}

	@Test
	public void addTracks() {
		final Tracks tracks = new Tracks(loc, "creator", Direction.EAST, Percentile.ONE, 0, null);
		loc.add(tracks);
		assertArrayEquals(new Tracks[]{tracks}, loc.tracks().toArray());
	}


	@Test
	public void removeTracks() {
		final Tracks tracks = new Tracks(loc, "creator", Direction.EAST, Percentile.ONE, 0, null);
		loc.add(tracks);
		loc.remove(tracks);
		assertEquals(0, loc.tracks().count());
	}

	@Test
	public void lightLevelChange() {
		TestHelper.light(loc);
		loc.notify(ContentStateChange.LIGHT_MODIFIED);
		assertEquals(Percentile.ONE, loc.emission(Emission.LIGHT));
	}
}
