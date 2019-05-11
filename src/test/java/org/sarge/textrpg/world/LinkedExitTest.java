package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.LinkedExit.ReversePolicy;

public class LinkedExitTest {
	private LinkedExit descriptor;
	private Link link;
	private Location start, end;

	@BeforeEach
	public void before() {
		start = mock(Location.class);
		end = mock(Location.class);
		link = RouteLink.of(Route.LANE);
		descriptor = new LinkedExit(start, Direction.EAST, link, "dest", ReversePolicy.INVERSE, null);
	}

	@Test
	public void constructor() {
		assertEquals("dest", descriptor.destination());
		assertEquals(ReversePolicy.INVERSE, descriptor.policy());
	}

	@Test
	public void invalidReverseDirection() {
		assertThrows(IllegalArgumentException.class, () -> new LinkedExit(start, Direction.EAST, link, "dest", ReversePolicy.ONE_WAY, Direction.NORTH));
	}

	@Test
	public void exit() {
		final Exit exit = descriptor.exit(end);
		assertEquals(Direction.EAST, exit.direction());
		assertEquals(link, exit.link());
		assertEquals(end, exit.destination());
	}

	@Test
	public void reverse() {
		final Exit exit = descriptor.reverse();
		assertEquals(Direction.WEST, exit.direction());
		assertEquals(link, exit.link());
		assertEquals(start, exit.destination());
	}

	@Test
	public void reverseSimple() {
		descriptor = new LinkedExit(start, Direction.EAST, link, "dest", ReversePolicy.SIMPLE, null);
		final Exit exit = descriptor.reverse();
		assertEquals(Direction.WEST, exit.direction());
		assertEquals(Link.DEFAULT, exit.link());
		assertEquals(start, exit.destination());
	}

	@Test
	public void reverseWithDirection() {
		descriptor = new LinkedExit(start, Direction.EAST, link, "dest", ReversePolicy.SIMPLE, Direction.NORTH);
		final Exit exit = descriptor.reverse();
		assertEquals(Direction.NORTH, exit.direction());
		assertEquals(Link.DEFAULT, exit.link());
		assertEquals(start, exit.destination());
	}

	@Test
	public void reverseOneWay() {
		descriptor = new LinkedExit(start, Direction.EAST, link, "dest", ReversePolicy.ONE_WAY, null);
		assertThrows(IllegalStateException.class, () -> descriptor.reverse());
	}
}
