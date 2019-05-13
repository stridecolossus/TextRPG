package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.DefaultMovementRequirement;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.MovementController;
import org.sarge.textrpg.world.Route;

public class ClimbActionTest extends ActionTestBase {
	private ClimbAction action;
	private MovementController mover;
	private Exit exit;
	private WorldObject climb;

	@BeforeEach
	public void before() {
		// Create climb link
		final ClimbLink link = mock(ClimbLink.class);
		climb = mock(WorldObject.class);
		when(link.route()).thenReturn(Route.NONE);
		when(link.controller()).thenReturn(Optional.of(climb));
		when(link.difficulty()).thenReturn(Percentile.HALF);
		when(link.modifier()).thenReturn(2f);
		when(actor.perceives(climb)).thenReturn(true);

		// Create exit
		exit = Exit.of(Direction.UP, link, loc);
		when(loc.exits()).thenReturn(ExitMap.of(exit));

		// Create action
		mover = mock(MovementController.class);
		action = new ClimbAction(mover, mock(DefaultMovementRequirement.class), skill);
	}

	@Test
	public void climb() throws ActionException {
		addRequiredSkill();
		final Response response = action.climb(actor, climb);
		assertEquals(Response.DISPLAY_LOCATION, response);
		verify(mover).move(actor, exit, 1);
	}

	@Test
	public void climbNotFound() throws ActionException {
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		TestHelper.expect("climb.invalid.object", () -> action.climb(actor, climb));
	}

	@Test
	public void climbInvalidObject() throws ActionException {
		TestHelper.expect("climb.invalid.object", () -> action.climb(actor, mock(WorldObject.class)));
	}

	@Test
	public void climbNotSafe() throws ActionException {
		actor.settings().set(PlayerSettings.Setting.CLIMB_SAFE, true);
		TestHelper.expect("climb.not.safe", () -> action.climb(actor, climb));
	}

	@Test
	public void climbFailed() throws ActionException {
		// final Response response = action.climb(actor, climb);
		// TODO
	}
}
