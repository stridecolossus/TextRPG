package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Percentile;

public class TrailTest {
	private Trail trail;
	private Location loc;

	@BeforeEach
	public void before() {
		trail = new Trail();
		loc = mock(Location.class);
	}

	@Test
	public void constructor() {
		assertEquals(null, trail.previous());
	}

	/**
	 * Adds some tracks.
	 */
	private Tracks addTracks() {
		final Tracks tracks = new Tracks(loc, "creator", Direction.EAST, Percentile.ONE, 0, null);
		trail.add(tracks);
		return tracks;
	}

	@Test
	public Tracks add() {
		final Tracks tracks = addTracks();
		assertEquals(tracks, trail.previous());
		assertEquals(1, loc.tracks().count());
		return tracks;
	}

	@Test
	public void addLinkPrevious() {
		final Tracks prev = addTracks();
		final Tracks next = new Tracks(loc, "creator", Direction.EAST, Percentile.ONE, 0, prev);
		trail.add(next);
		assertEquals(next, trail.previous());
		assertEquals(next, prev.next());
	}

	@Test
	public void stop() {
		addTracks();
		trail.stop();
		assertEquals(null, trail.previous());
	}

	@Test
	public void stopEmptyTrail() {
		trail.stop();
		assertEquals(null, trail.previous());
	}

	@Test
	public void prune() {
		final Tracks tracks = addTracks();
		trail.prune(1);
		assertEquals(null, trail.previous());
		verify(loc).remove(tracks);
	}

	@Test
	public void pruneMultipleSegments() {
		final Tracks tracks = addTracks();
		trail.stop();
		addTracks();
		trail.prune(1);
		assertEquals(null, trail.previous());
		verify(loc, times(2)).remove(tracks);
	}

	@Test
	public void pruneExpiry() {
		final Tracks prev = addTracks();
		final Tracks later = new Tracks(loc, "creator", Direction.EAST, Percentile.ONE, 1, prev);
		trail.add(later);
		trail.prune(1);
		assertEquals(later, trail.previous());
	}

	@Test
	public void clear() {
		final Tracks tracks = addTracks();
		trail.stop();
		trail.clear();
		verify(loc).remove(tracks);
	}
}
