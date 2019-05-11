package org.sarge.textrpg.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.entity.Race.Behaviour;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Direction;

public class DefaultNotificationHandlerTest extends ActionTestBase {
	private Notification.Handler handler;
	private BehaviourController controller;
	private Entity target;

	@BeforeEach
	public void before() {
		// Init actor
		final Race race = new Race.Builder("race").build();
		when(actor.descriptor().race()).thenReturn(race);
		target = mock(Entity.class);

		// Create controller
		controller = mock(BehaviourController.class);
		handler = new DefaultNotificationHandler(controller);
	}

	/**
	 * Initialises the race of the actor.
	 */
	private void init(Race race) {
		when(actor.descriptor().race()).thenReturn(race);
	}

	@Test
	public void init() {
		handler.init(actor);
		verify(controller).revert(actor);
	}

	@Test
	public void lightIgnored() {
		handler.light(actor);
		verifyZeroInteractions(controller);
	}

	@Test
	public void lightFlee() {
		final Race race = new Race.Builder("race").behaviour(Behaviour.Flag.STARTLED).build();
		init(race);
		handler.light(actor);
		verify(controller).flee(actor);
	}

	/**
	 * Generates an arrival notification.
	 */
	private void arrival() {
		handler.handle(new MovementNotification(target, Direction.EAST, true), actor);
	}

	@Test
	public void arrivalIgnored() {
		arrival();
		verifyZeroInteractions(controller);
	}

	@Test
	public void arrivalFlee() {
		final Race race = new Race.Builder("race").behaviour(Behaviour.Flag.STARTLED).build();
		init(race);
		arrival();
		verify(controller).flee(actor);
	}

	@Test
	public void arrivalAttack() {
		final Race race = new Race.Builder("race").aggression(Percentile.ONE).build();
		init(race);
		arrival();
		verify(controller).attack(actor, target);
	}

	@Test
	public void combat() {
		final CombatNotification notification = new CombatNotification(mock(Entity.class), CombatNotification.Type.ATTACKED, Damage.Type.COLD, 42);
		handler.handle(notification, actor);
		verify(controller).flee(actor);
	}
}
