package org.sarge.textrpg.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.MovementController;
import org.sarge.textrpg.world.Property;

public class SavePointMovementListenerTest {
	private MovementController.Listener listener;
	private Entity actor;

	@BeforeEach
	public void before() {
		final Location loc = mock(Location.class);
		actor = mock(Entity.class);
		when(actor.location()).thenReturn(loc);
		listener = new SavePointMovementListener();
	}

	@Test
	public void update() {
		when(actor.isPlayer()).thenReturn(true);
		when(actor.location().isProperty(Property.SAVE_POINT)).thenReturn(true);
		listener.update(actor, null, null);
		// TODO
	}

	@Test
	public void updateNotPlayer() {
		when(actor.location().isProperty(Property.SAVE_POINT)).thenReturn(true);
		listener.update(actor, null, null);
		// TODO
	}

	@Test
	public void updateNotSavePoint() {
		when(actor.isPlayer()).thenReturn(true);
		listener.update(actor, null, null);
		// TODO
	}
}
