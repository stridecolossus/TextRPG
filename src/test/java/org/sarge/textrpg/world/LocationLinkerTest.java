package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationLinkerTest {
	private Location.Linker linker;
	private Location loc;

	@BeforeEach
	public void before() {
		// Create a location
		loc = mock(Location.class);
		when(loc.name()).thenReturn("dest");
		when(loc.terrain()).thenReturn(Terrain.DESERT);

		// Create linker
		linker = new Location.Linker();
	}

	@Test
	public void invalidReverseExitWrapper() {
		assertThrows(IllegalArgumentException.class, () -> new LinkedExit(loc, Direction.EAST, Link.DEFAULT, "dest", LinkedExit.ReversePolicy.ONE_WAY, Direction.DOWN));
	}

	@Test
	public void link() {
		// Link to destination
		final LinkedExit exit = new LinkedExit(loc, Direction.EAST, Link.DEFAULT, "dest", LinkedExit.ReversePolicy.ONE_WAY, null);
		linker.add(exit);
		linker.add(loc);
		linker.link();

		// Check linked
		final Exit expected = new Exit(Direction.EAST, Link.DEFAULT, loc);
		verify(loc).add(expected);
		verify(loc).complete();
	}

	@Test
	public void linkReverseLink() {
		// Add reverse link to/from self
		final Link link = mock(Link.class);
		when(link.route()).thenReturn(Route.CORRIDOR);
		final LinkedExit exit = new LinkedExit(loc, Direction.EAST, link, "dest", LinkedExit.ReversePolicy.SIMPLE, Direction.NORTH);
		linker.add(exit);
		linker.add(loc);
		linker.link();

		// Check links
		verify(loc).add(new Exit(Direction.EAST, link, loc));
		verify(loc).add(new Exit(Direction.NORTH, Link.DEFAULT, loc));
		verify(loc).complete();
	}

	@Test
	public void linkUnknownDestination() {
		final LinkedExit exit = new LinkedExit(loc, Direction.EAST, Link.DEFAULT, "cobblers", LinkedExit.ReversePolicy.ONE_WAY, null);
		linker.add(exit);
		assertThrows(IllegalArgumentException.class, () -> linker.link());
	}
}
