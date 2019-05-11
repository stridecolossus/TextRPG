package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.ExitMap.MutableExitMap;

public class ArrayExitMapTest {
	private ArrayExitMap exits;
	private Location dest;

	@BeforeEach
	public void before() {
		// Create destination
		dest = mock(Location.class);
		when(dest.terrain()).thenReturn(Terrain.DESERT);

		// Create exits
		final MutableExitMap mutable = new MutableExitMap();
		mutable.add(new Exit(Direction.WEST, Link.DEFAULT, dest));
		mutable.add(new Exit(Direction.EAST, Link.DEFAULT, dest));
		exits = new ArrayExitMap(mutable);
	}

	@Test
	public void constructor() {
		assertEquals(false, exits.isEmpty());
	}

	@Test
	public void find() {
		for(Direction dir : Direction.values()) {
			switch(dir) {
			case EAST:
			case WEST:
				assertEquals(Optional.of(new Exit(dir, Link.DEFAULT, dest)), exits.find(dir));
				break;

			default:
				assertEquals(Optional.empty(), exits.find(Direction.NORTH));
				break;
			}
		}
	}

	@Test
	public void stream() {
		final Exit east = new Exit(Direction.EAST, Link.DEFAULT, dest);
		final Exit west = new Exit(Direction.WEST, Link.DEFAULT, dest);
		assertNotNull(exits.stream());
		assertArrayEquals(new Exit[]{east, west}, exits.stream().toArray());
	}

	@Test
	public void buildEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new ArrayExitMap(ExitMap.EMPTY));
	}
}
