package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class DirectionTest {
	@Test
	public void reverse() {
		assertEquals(Direction.SOUTH, Direction.NORTH.reverse());
		assertEquals(Direction.NORTH, Direction.SOUTH.reverse());
		assertEquals(Direction.EAST, Direction.WEST.reverse());
		assertEquals(Direction.WEST, Direction.EAST.reverse());
		assertEquals(Direction.UP, Direction.DOWN.reverse());
		assertEquals(Direction.DOWN, Direction.UP.reverse());
	}

	@ParameterizedTest
	@EnumSource(Direction.class)
	public void mnenomic(Direction dir) {
		final String expected = dir.name().substring(0, 1).toLowerCase();
		assertEquals(expected, dir.mnemonic());
	}

	@ParameterizedTest
	@EnumSource(value=Direction.class, names={"NORTH", "SOUTH", "EAST", "WEST"})
	public void isCardinal(Direction dir) {
		assertTrue(dir.isCardinal());
	}

	@ParameterizedTest
	@EnumSource(value=Direction.class, mode=EnumSource.Mode.EXCLUDE, names={"NORTH", "SOUTH", "EAST", "WEST"})
	public void isNotCardinal(Direction dir) {
		assertFalse(dir.isCardinal());
	}

	@Test
	public void path() {
		final List<Direction> path = Direction.path("nsew");
		assertEquals(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST), path);
	}
}
