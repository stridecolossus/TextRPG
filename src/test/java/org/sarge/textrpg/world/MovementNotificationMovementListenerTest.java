package org.sarge.textrpg.world;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.entity.MovementNotification;
import org.sarge.textrpg.util.Percentile;

/**
 * @author Sarge
 */
class MovementNotificationMovementListenerTest extends ActionTestBase {
	private MovementController.Listener listener;
	private EmissionController controller;

	@BeforeEach
	public void before() {
		controller = mock(EmissionController.class);
		listener = new MovementNotificationMovementListener(controller);
		when(actor.visibility()).thenReturn(Percentile.HALF);
	}

	private static Location location() {
		final Location loc = mock(Location.class);
		when(loc.contents()).thenReturn(new Contents());
		return loc;
	}

	@Test
	public void update() {
		final Location prev = location();
		final Location dest = location();
		final Exit exit = new Exit(Direction.EAST, Link.DEFAULT, dest);
		listener.update(actor, exit, prev);
		verify(controller).broadcast(actor, loc, new MovementNotification(actor, Direction.EAST, false), Percentile.HALF);
		verify(controller).broadcast(actor, loc, new MovementNotification(actor, Direction.WEST, true), Percentile.HALF);
	}
}
