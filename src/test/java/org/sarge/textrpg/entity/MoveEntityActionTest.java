package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.world.Direction;

public class MoveEntityActionTest extends ActionTest {
	@Test
	public void execute() throws ActionException {
		// Create follower
		final Follower follower = mock(Follower.class);
		when(follower.next(actor)).thenReturn(Direction.EAST);

		// Invoke action
		final MoveEntityAction action = new MoveEntityAction(ctx, follower, true);
		final boolean stop = action.execute(actor);
		verify(follower).next(actor);
		verify(ctx.getMovementController()).move(ctx, actor, Direction.EAST, 1, false);
		assertEquals(false, stop);
		
		// Terminate follower and check action is stopped
		when(follower.next(actor)).thenReturn(null);
		assertEquals(true, action.execute(actor));
	}
}
