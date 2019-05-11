package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.FleeAction;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.MovementController;
import org.sarge.textrpg.world.MovementManager;

public class BehaviourControllerTest extends ActionTestBase {
	private BehaviourController controller;
	private MovementController mover;
	private FleeAction flee;

	@BeforeEach
	public void before() {
		// Init idle actor
		final Race race = new Race.Builder("race").build();
		when(actor.descriptor().race()).thenReturn(race);

		// Create flee action
		flee = mock(FleeAction.class);
		when(flee.flee(actor)).thenReturn(Response.EMPTY);

		// Create controller
		mover = mock(MovementController.class);
		controller = new BehaviourController(mover, flee);
	}

	@Test
	public void revert() throws ActionException {
		// Create a movement manager
		final Exit exit = new Exit(Direction.EAST, Link.DEFAULT, loc);
		final MovementManager manager = ignore -> Optional.of(exit);

		// Init race
		final Race race = new Race.Builder("race").movement(manager).period(Duration.ofMinutes(1)).build();
		when(actor.descriptor().race()).thenReturn(race);

		// Revert to default behaviour
		controller.revert(actor);

		// Check induction started
		final ArgumentCaptor<Induction.Instance> captor = ArgumentCaptor.forClass(Induction.Instance.class);
		verify(actor.manager().induction()).start(captor.capture());

		// Check induction
		final Induction.Instance instance = captor.getValue();
		assertEquals(true, instance.descriptor().isFlag(Induction.Flag.REPEATING));

		// Complete induction iteration
		instance.induction().complete();
		verify(mover).move(actor, exit, 1);
	}

	@Test
	public void revertIdle() throws ActionException {
		controller.revert(actor);
		assertEquals(0, actor.manager().queue().size());
	}

	@Test
	public void flee() {
		controller.flee(actor);
		verify(flee).flee(actor);
		assertEquals(0, actor.manager().queue().size());
	}

	@Test
	public void attack() {
		// TODO
		//controller.attack(actor, target);
	}
}
