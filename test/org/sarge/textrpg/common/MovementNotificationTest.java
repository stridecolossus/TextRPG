package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Notification.Handler;
import org.sarge.textrpg.world.Direction;

public class MovementNotificationTest {
	private MovementNotification notification;
	private Actor actor;
	
	@Before
	public void before() {
		actor = mock(Actor.class);
		when(actor.toString()).thenReturn("actor");
		notification = new MovementNotification(actor, true, Direction.WEST, false);
	}
	
	@Test
	public void constructor() {
		assertEquals(actor, notification.getActor());
		assertEquals(true, notification.isArrival());
		assertEquals(Optional.of(Direction.WEST), notification.getDirection());
	}
	
	@Test
	public void describe() {
		final Description desc = notification.describe();
		assertNotNull(desc);
		assertEquals("{actor}", desc.get("name"));
		assertEquals("{movement.notification.arrives}", desc.get("arrival"));
		assertEquals("{movement.notification.west}", desc.get("dir"));
	}

	@Test
	public void accept() {
		final Handler handler = mock(Handler.class);
		final MovementNotification move = new MovementNotification(mock(Actor.class), true, Direction.DOWN, false);
		move.accept(handler);
		verify(handler).handle(move);
	}
}
