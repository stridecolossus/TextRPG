package org.sarge.textrpg.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.world.CurrentLink;
import org.sarge.textrpg.world.CurrentLink.Current;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.RiverController;

public class WaterMovementListenerTest extends ActionTestBase {
	private WaterMovementListener listener;
	private SwimmingController controller;
	private RiverController river;

	@BeforeEach
	public void before() {
		controller = mock(SwimmingController.class);
		river = mock(RiverController.class);
		listener = new WaterMovementListener(controller, river);
	}

	@Test
	public void updateNotWater() {
		listener.update(actor, null, null);
		verifyZeroInteractions(controller);
	}

	@Test
	public void updateStartSwimming() {
		when(loc.isWater()).thenReturn(true);
		listener.update(actor, null, null);
		verify(controller).start(actor);
		verifyZeroInteractions(river);
	}

	@Test
	public void updateRiverCurrent() {
		when(loc.isWater()).thenReturn(true);
		final Exit exit = new Exit(Direction.EAST, new CurrentLink(Current.MEDIUM), loc);
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		listener.update(actor, null, null);
		verify(controller).start(actor);
		verify(river).add(actor, exit);
	}

	@Test
	public void updateFrozenWater() {
		when(loc.isWater()).thenReturn(true);
		when(loc.isFrozen()).thenReturn(true);
		listener.update(actor, null, null);
		verifyZeroInteractions(controller);
	}

	@Test
	public void updateAlreadySwimming() {
		when(loc.isWater()).thenReturn(true);
		when(loc.isFrozen()).thenReturn(true);
		when(actor.model().stance()).thenReturn(Stance.SWIMMING);
		listener.update(actor, null, null);
		verifyZeroInteractions(controller);
	}

	@Test
	public void updateNotWaterStopSwimming() {
		when(actor.model().stance()).thenReturn(Stance.SWIMMING);
		listener.update(actor, null, null);
		verify(controller).stop(actor);
	}
}
