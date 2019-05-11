package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.Direction;

public class MovementNotificationTest {
	private Entity actor;

	@BeforeEach
	public void before() {
		actor = mock(Entity.class);
		when(actor.name()).thenReturn("name");
	}

	@Test
	public void describe() {
		final Notification notification = new MovementNotification(actor, Direction.EAST, true);
		final Description expected = new Description.Builder("notification.movement.arrived")
			.add("dir", Direction.EAST)
			.add("name", "name")
			.build();
		assertEquals(expected, notification.describe());
	}

	@Test
	public void describeNullDirection() {
		final Notification notification = new MovementNotification(actor, null, false);
		final Description expected = new Description.Builder("notification.movement.disappeared")
			.add("name", "name")
			.build();
		assertEquals(expected, notification.describe());
	}
}
