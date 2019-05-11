package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.Follower.FollowerModel;
import org.sarge.textrpg.entity.Leader.LeaderModel;

public class FollowerTest {
	private Follower follower;
	private Leader leader;

	@BeforeEach
	public void before() {
		// Create follower
		final FollowerModel followerModel = new FollowerModel();
		follower = mock(Follower.class);
		when(follower.follower()).thenReturn(followerModel);

		// Create leader
		final LeaderModel leaderModel = new LeaderModel();
		leader = mock(Leader.class);
		when(leader.leader()).thenReturn(leaderModel);
	}

	@Test
	public void constructor() {
		assertEquals(false, follower.follower().isFollowing());
		assertEquals(false, follower.follower().isFollowing(leader));
		assertNotNull(leader.leader().followers());
		assertEquals(0, leader.leader().followers().count());
	}

	@Test
	public void follow() {
		Follower.follow(follower, leader);
		assertEquals(true, follower.follower().isFollowing());
		assertEquals(true, follower.follower().isFollowing(leader));
		assertEquals(1, leader.leader().followers().count());
		assertEquals(follower, leader.leader().followers().iterator().next());
	}

	@Test
	public void followAlreadyFollowing() {
		Follower.follow(follower, leader);
		assertThrows(IllegalArgumentException.class, () -> Follower.follow(follower, leader));
	}

	@Test
	public void stop() {
		Follower.follow(follower, leader);
		final var prev = Follower.stop(follower);
		assertEquals(false, follower.follower().isFollowing());
		assertEquals(false, follower.follower().isFollowing(leader));
		assertEquals(0, leader.leader().followers().count());
		assertEquals(leader, prev);
	}

	@Test
	public void stopNotFollowing() {
		assertThrows(IllegalArgumentException.class, () -> Follower.stop(follower));
	}

	@Test
	public void clear() {
		Follower.follow(follower, leader);
		Follower.clear(follower);
		assertEquals(false, follower.follower().isFollowing());
		assertEquals(0, leader.leader().followers().count());
	}

	@Test
	public void clearNotFollowing() {
		Follower.clear(follower);
	}
}
