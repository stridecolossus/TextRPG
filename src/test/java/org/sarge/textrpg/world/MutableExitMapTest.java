package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.CurrentLink.Current;
import org.sarge.textrpg.world.ExitMap.MutableExitMap;

public class MutableExitMapTest {
	private MutableExitMap exits;
	private Location dest;

	@BeforeEach
	public void before() {
		exits = new MutableExitMap();
		dest = mock(Location.class);
		when(dest.terrain()).thenReturn(Terrain.DESERT);
	}

	@Test
	public void add() {
		final Exit exit = new Exit(Direction.EAST, Link.DEFAULT, dest);
		exits.add(exit);
		assertEquals(Optional.of(exit), exits.find(Direction.EAST));
	}

	@Test
	public void addInvalid() {
		final CurrentLink link = new CurrentLink(Current.FAST);
		exits.add(new Exit(Direction.EAST, link, dest));
		assertThrows(IllegalArgumentException.class, () -> exits.add(new Exit(Direction.NORTH, link, dest)));
	}
}
