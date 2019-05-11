package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;

public class MovementManagerTest {
	@Nested
	class ManagerTests {
		private Location loc;
		private Exit exit;

		@BeforeEach
		public void before() {
			loc = mock(Location.class);
			exit = new Exit(Direction.EAST, Link.DEFAULT, loc);
			when(loc.exits()).thenReturn(ExitMap.of(exit));
			when(loc.terrain()).thenReturn(Terrain.FOREST);
		}

		@Test
		public void idle() {
			assertThrows(UnsupportedOperationException.class, () -> MovementManager.IDLE.next(null));
		}

		@Test
		public void random() {
			assertEquals(Optional.of(exit), MovementManager.RANDOM.next(loc));
		}

		@Test
		public void randomEmptyExits() {
			when(loc.exits()).thenReturn(ExitMap.EMPTY);
			assertEquals(Optional.empty(), MovementManager.RANDOM.next(loc));
		}

		@Test
		public void terrain() {
			final MovementManager manager = MovementManager.terrain(Set.of(Terrain.FOREST));
			assertEquals(Optional.of(exit), manager.next(loc));
		}

		@Test
		public void terrainEmptyExits() {
			final MovementManager manager = MovementManager.terrain(Set.of(Terrain.FOREST));
			when(loc.exits()).thenReturn(ExitMap.EMPTY);
			assertEquals(Optional.empty(), manager.next(loc));
		}

		@Test
		public void terrainNoneAvailable() {
			final MovementManager manager = MovementManager.terrain(Set.of(Terrain.FOREST));
			when(loc.terrain()).thenReturn(Terrain.DESERT);
			assertEquals(Optional.empty(), manager.next(loc));
		}

		@Test
		public void path() {
			final MovementManager manager = MovementManager.path(List.of(Direction.EAST, Direction.SOUTH));
			assertEquals(Optional.of(exit), manager.next(loc));
			assertEquals(Optional.empty(), manager.next(loc));
		}
	}

	@Nested
	class LoaderTests {
		@Test
		public void loadRandom() {
			final Element xml = new Element.Builder("xml").attribute("type", "random").build();
			assertEquals(MovementManager.RANDOM, MovementManager.load(xml));
		}

		@Test
		public void loadTerrain() {
			final Element xml = new Element.Builder("xml")
				.attribute("type", "terrain")
				.add("grassland")
				.add("farmland")
				.build();
			final MovementManager manager = MovementManager.load(xml);
			assertNotNull(manager);
		}

		@Test
		public void loadPath() {
			final Element xml = new Element.Builder("xml")
				.attribute("type", "terrain")
				.attribute("path", "nsew")
				.build();
			final MovementManager manager = MovementManager.load(xml);
			assertNotNull(manager);
		}
	}
}
