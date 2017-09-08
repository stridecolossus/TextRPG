package org.sarge.textrpg.world;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.entity.Follower;
import org.sarge.textrpg.object.TrackedContents;

public class TracksFollowerTest {
	private Follower follower;
	private Actor actor;
	private Location loc;
	private TrackedContents contents;

	@Before
	public void before() {
		// Create tracks follower
		follower = new TracksFollower("tracks", 100);
		
		// Create actor
		actor = mock(Actor.class);
		when(actor.perceives(any(Hidden.class))).thenReturn(true);
		
		// Create location
		loc = mock(Location.class);
		contents = new TrackedContents();
		when(loc.contents()).thenReturn(contents);
	}
	
	/*
	@Test
	public void next() throws ActionException {
		contents.add(new Tracks("tracks", Direction.EAST, 1f, 2L));
		assertEquals(Direction.EAST, follower.next(actor, loc));
	}
	
	@Test
	public void nextNoTracks() {
		assertEquals(null, follower.next(actor, loc));
	}
	
	@Test
	public void nextDifferentTracks() {
		contents.add(new Tracks("other", Direction.EAST, 1f, 2L));
		assertEquals(null, follower.next(actor, loc));
	}
	
	@Test
	public void nextInsufficientLevel() {
		contents.add(new Tracks("tracks", Direction.EAST, 0f, 2L));
		assertEquals(null, follower.next(actor, loc));
	}
	*/
}
