package org.sarge.textrpg.world;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.Percentile;

class EmissionNotificationMovementListenerTest {
	private MovementController.Listener listener;
	private EmissionController controller;
	private Entity actor;

	@BeforeEach
	public void before() {
		controller = mock(EmissionController.class);
		listener = new EmissionNotificationMovementListener(controller);
		final Location loc = mock(Location.class);
		actor = mock(Entity.class);
		when(actor.location()).thenReturn(loc);
		when(actor.emission(any(Emission.class))).thenReturn(Percentile.ZERO);
	}

	@Test
	public void update() {
		when(actor.emission(Emission.LIGHT)).thenReturn(Percentile.HALF);
		listener.update(actor, null, null);
		verify(controller).broadcast(actor, Set.of(new EmissionNotification(Emission.LIGHT, Percentile.HALF)));
	}

	@Test
	public void updateNone() {
		listener.update(actor, null, null);
		verifyZeroInteractions(controller);
	}
}
