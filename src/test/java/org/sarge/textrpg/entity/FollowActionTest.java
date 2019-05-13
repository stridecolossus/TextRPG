package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Leader.LeaderModel;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.*;
import org.sarge.textrpg.world.ExitMap.MutableExitMap;

public class FollowActionTest extends ActionTestBase {
	private FollowAction action;
	private FollowHelper helper;
	private CharacterEntity leader;
	private Location dest;

	@BeforeEach
	public void before() {
		// Create leader to follow
		leader = mock(CharacterEntity.class);
		when(leader.name()).thenReturn("leader");
		when(leader.leader()).thenReturn(mock(LeaderModel.class));

		// Create a destination to follow
		dest = mock(Location.class);
		when(dest.terrain()).thenReturn(Terrain.DESERT);
		when(dest.exits()).thenReturn(ExitMap.EMPTY);

		// Create action
		helper = mock(FollowHelper.class);
		action = new FollowAction(helper);
	}

	@Test
	public void followEntity() throws ActionException {
		// Follow leader
		final Response response = action.follow(actor, leader);
		assertEquals(Response.of(new Description("action.follow.entity", "leader")), response);

		// Check following
		verify(leader.leader()).add(actor);
		assertEquals(true, actor.follower().isFollowing(leader));
	}

	@Test
	public void followEntityAlreadyFollowing() throws ActionException {
		action.follow(actor, leader);
		TestHelper.expect("follow.already.following", () -> action.follow(actor, leader));
	}

	@Test
	public void followEntityFollowingOther() throws ActionException {
		final CharacterEntity other = mock(CharacterEntity.class);
		when(other.name()).thenReturn("other");
		when(other.leader()).thenReturn(mock(LeaderModel.class));
		action.follow(actor, other);
		TestHelper.expect("follow.following.other", () -> action.follow(actor, leader));
	}

	@Test
	public void stopFollowing() throws ActionException {
		action.follow(actor, leader);
		final Response response = action.follow(actor);
		assertEquals(Response.of(new Description("action.stop.follow", "leader")), response);
		verify(leader.leader()).remove(actor);
		assertEquals(false, actor.follower().isFollowing(leader));
	}

	@Test
	public void stopFollowingNotFollowing() throws ActionException {
		TestHelper.expect("stop.not.following", () -> action.follow(actor));
	}

	@Test
	public void followRoute() throws ActionException {
		// Add an exit with a route
		final Exit exit = Exit.of(Direction.EAST, dest);
		when(loc.exits()).thenReturn(ExitMap.of(exit));

		// Start following
		action.follow(actor, Route.ROAD);
		verify(helper).follow(eq(actor), eq(exit), any(), eq("route"));
	}

	@Test
	public void followRouteNone() throws ActionException {
		TestHelper.expect("follow.route.none", () -> action.follow(actor, Route.ROAD));
	}

	@Test
	public void followRouteAmbiguous() throws ActionException {
		final MutableExitMap exits = new MutableExitMap();
		exits.add(Exit.of(Direction.EAST, dest));
		exits.add(Exit.of(Direction.WEST, dest));
		when(loc.exits()).thenReturn(exits);
		TestHelper.expect("follow.route.ambiguous", () -> action.follow(actor, Route.ROAD));
	}

	@Test
	public void followRouteNotPerceived() throws ActionException {
		final Link link = new HiddenLink(new ExtendedLink.Properties(), "name", Percentile.ZERO);
		final Exit exit = Exit.of(Direction.EAST, link, dest);
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		TestHelper.expect("follow.route.none", () -> action.follow(actor, Route.ROAD));
	}

	@Test
	public void followRouteDirection() throws ActionException {
		final Exit exit = Exit.of(Direction.EAST, dest);
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		action.follow(actor, Route.ROAD);
	}

	@Test
	public void followRouteInvalidDirection() throws ActionException {
		TestHelper.expect("follow.invalid.direction", () -> action.follow(actor, Direction.EAST));
	}
}