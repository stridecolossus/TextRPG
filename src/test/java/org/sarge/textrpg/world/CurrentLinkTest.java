package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.CurrentLink.Current;

public class CurrentLinkTest {
	private CurrentLink link;

	@BeforeEach
	public void before() {
		link = new CurrentLink(Current.RAPIDS);
	}

	@Test
	public void constructor() {
		assertEquals(true, link.isTraversable());
		assertEquals(Route.RIVER, link.route());
		assertEquals(Current.RAPIDS, link.current());
	}

	@Test
	public void wrap() {
		assertEquals("!dir!", link.wrap("dir"));
	}

	@Test
	public void wrapDefault() {
		link = new CurrentLink(Current.MEDIUM);
		assertEquals("~dir~", link.wrap("dir"));
	}

	@Test
	public void find() {
		// Create water location
		final Location loc = mock(Location.class);
		when(loc.isWater()).thenReturn(true);

		// Create current exit
		final Exit exit = new Exit(Direction.EAST, link, loc);
		final ExitMap exits = ExitMap.of(exit);
		when(loc.exits()).thenReturn(exits);

		// Check current is found
		assertEquals(Optional.of(exit), CurrentLink.find(loc));
	}

	@Test
	public void findNone() {
		final Location loc = mock(Location.class);
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		assertEquals(Optional.empty(), CurrentLink.find(loc));
	}
}
