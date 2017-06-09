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
		assertEquals("n", Direction.NORTH.getMnenomic());
		assertEquals("s", Direction.SOUTH.getMnenomic());
		assertEquals("e", Direction.EAST.getMnenomic());
		assertEquals("w", Direction.WEST.getMnenomic());
		assertEquals("u", Direction.UP.getMnenomic());
		assertEquals("d", Direction.DOWN.getMnenomic());
	}
}
