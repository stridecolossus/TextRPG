package org.sarge.textrpg.world;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationBroadcasterTest {
	private LocationBroadcaster broadcaster;

	@BeforeEach
	public void before() {
		broadcaster = new LocationBroadcaster(1);
	}

	@Test
	public void visit() {
		// Create a linked location
		final Location start = mock(Location.class);
		final Location dest = mock(Location.class);
		when(start.exits()).thenReturn(ExitMap.of(new Exit(Direction.EAST, Link.DEFAULT, dest)));
		when(dest.exits()).thenReturn(ExitMap.of(new Exit(Direction.WEST, Link.DEFAULT, start)));

		// Invoke and check only neighbour visited
		final LocationBroadcaster.Visitor vis = mock(LocationBroadcaster.Visitor.class);
		broadcaster.visit(start, vis);
		verify(vis).visit(new Exit(Direction.EAST, Link.DEFAULT, dest), 1);
		verifyNoMoreInteractions(vis);
	}
}
