package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.ExitMap.MutableExitMap;

public class ExitMapTest {
	private Exit east, west;
	private Location dest;

	@BeforeEach
	public void before() {
		dest = mock(Location.class);
		east = Exit.of(Direction.EAST, dest);
		west = Exit.of(Direction.WEST, dest);
	}

	@Nested
	class EmptyTests {
		@Test
		public void empty() {
			assertEquals(true, ExitMap.EMPTY.isEmpty());
			assertEquals(0, ExitMap.EMPTY.stream().count());
		}
	}

	@Nested
	class MutableExitsTests {
		private MutableExitMap exits;

		@BeforeEach
		public void before() {
			exits = new MutableExitMap();
		}

		@Test
		public void constructor() {
			assertEquals(true, exits.isEmpty());
			assertEquals(0, exits.stream().count());
		}

		@Test
		public void add() {
			exits.add(west);
			exits.add(east);
			assertEquals(false, exits.isEmpty());
			assertEquals(Set.of(west, east), exits.stream().collect(toSet()));
			assertEquals(Optional.of(east), exits.find(Direction.EAST));
			assertEquals(Optional.of(west), exits.find(Direction.WEST));
		}
	}

	@Nested
	class CompactTests {
		private MutableExitMap exits;

		@BeforeEach
		public void before() {
			exits = new MutableExitMap();
		}

		@Test
		public void empty() {
			final ExitMap empty = exits.compact();
			assertEquals(true, empty.isEmpty());
			assertEquals(0, empty.stream().count());
		}

		@Test
		public void single() {
			final ExitMap single = exits.add(east).compact();
			assertEquals(false, single.isEmpty());
			assertArrayEquals(new Exit[]{east}, single.stream().toArray());
			assertEquals(Optional.of(east), single.find(Direction.EAST));
			assertEquals(Optional.empty(), single.find(Direction.WEST));
		}

		@Test
		public void array() {
			final ExitMap array = exits.add(west).add(east).compact();
			assertEquals(false, array.isEmpty());
			assertArrayEquals(new Exit[]{east, west}, array.stream().toArray());
			assertEquals(Optional.of(east), array.find(Direction.EAST));
			assertEquals(Optional.of(west), array.find(Direction.WEST));
		}

		@Test
		public void cardinal() {
			final ExitMap array = exits
				.add(west)
				.add(east)
				.add(Exit.of(Direction.SOUTH, dest))
				.add(Exit.of(Direction.NORTH, dest))
				.compact();
			assertEquals(false, array.isEmpty());
			assertArrayEquals(new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}, array.stream().map(Exit::direction).toArray());
		}
	}
}
