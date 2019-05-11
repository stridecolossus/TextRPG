package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.ExitMap.MutableExitMap;

public class ExitMapTest {
	private Exit exit;
	private MutableExitMap map;

	@BeforeEach
	public void before() {
		exit = new Exit(Direction.EAST, Link.DEFAULT, mock(Location.class));
		map = new MutableExitMap();
	}

	@Test
	@DisplayName("Create an exit-map with a single entry")
	public void of() {
		final ExitMap map = ExitMap.of(exit);
		assertNotNull(map);
		assertEquals(false, map.isEmpty());
		assertEquals(1, map.stream().count());
		assertEquals(Optional.of(exit), map.find(Direction.EAST));
	}

	@Test
	@DisplayName("Non-mutable map returns self")
	public void self() {
		final ExitMap map = mock(ExitMap.class);
		assertEquals(map, ExitMap.of(map));
	}

	@Test
	public void empty() {
		assertEquals(ExitMap.EMPTY, ExitMap.of(map));
		assertEquals(true, ExitMap.EMPTY.isEmpty());
	}

	@Test
	@DisplayName("Converts single exit to custom implementation")
	public void single() {
		map.add(exit);
		final ExitMap result = ExitMap.of(map);
		assertEquals(false, map.isEmpty());
		assertEquals(1, result.stream().count());
		assertEquals(Optional.of(exit), result.find(Direction.EAST));
	}

	@Test
	@DisplayName("Converts multiple exits to custom array implementation")
	public void multiple() {
		map.add(exit);
		map.add(new Exit(Direction.WEST, Link.DEFAULT, mock(Location.class)));
		final ExitMap result = ExitMap.of(map);
		assertEquals(false, result.isEmpty());
		assertTrue(result instanceof ArrayExitMap);
		assertEquals(2, result.stream().count());
	}
}
