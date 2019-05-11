package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Percentile;

public class TracksTest {
	private Tracks tracks;
	private Location loc;

	@BeforeEach
	public void before() {
		loc = mock(Location.class);
		tracks = new Tracks(loc, "creator", Direction.EAST, Percentile.HALF, 42, null);
	}

	@Test
	public void constructor() {
		assertEquals("creator", tracks.creator());
		assertEquals(Direction.EAST, tracks.direction());
		assertEquals(Percentile.HALF, tracks.visibility());
		assertEquals(42L, tracks.created());
		assertEquals(null, tracks.next());
		assertEquals(null, tracks.previous());
	}

	@Test
	public void conceal() {
		tracks.conceal(Percentile.HALF);
		assertEquals(Percentile.of(25), tracks.visibility());
	}

	@Test
	public void previous() {
		final Tracks next = new Tracks(loc, "creator", Direction.EAST, Percentile.HALF, 42, tracks);
		assertEquals(next, tracks.next());
		assertEquals(null, tracks.previous());
		assertEquals(null, next.next());
		assertEquals(tracks, next.previous());
	}

	@Test
	public void remove() {
		final Tracks middle = new Tracks(loc, "creator", Direction.EAST, Percentile.HALF, 42, tracks);
		final Tracks last = new Tracks(loc, "creator", Direction.EAST, Percentile.HALF, 42, middle);
		middle.remove();
		assertEquals(null, tracks.next());
		assertEquals(null, middle.next());
		assertEquals(null, middle.previous());
		assertEquals(null, last.previous());
	}
}
