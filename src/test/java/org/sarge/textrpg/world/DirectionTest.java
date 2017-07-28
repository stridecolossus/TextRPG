package org.sarge.textrpg.world;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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

	@Test
	public void getMnenomic() {
		assertEquals("n", Direction.NORTH.getMnemonic());
		assertEquals("s", Direction.SOUTH.getMnemonic());
		assertEquals("e", Direction.EAST.getMnemonic());
		assertEquals("w", Direction.WEST.getMnemonic());
		assertEquals("u", Direction.UP.getMnemonic());
		assertEquals("d", Direction.DOWN.getMnemonic());
	}
}
