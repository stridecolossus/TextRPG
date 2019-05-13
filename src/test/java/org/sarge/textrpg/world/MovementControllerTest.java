package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.MovementMode;
import org.sarge.textrpg.entity.Transaction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.MovementController.Listener;
import org.sarge.textrpg.world.MovementController.Requirement;
import org.sarge.textrpg.world.MovementController.Result;

public class MovementControllerTest extends ActionTestBase {
	private MovementController controller;
	private Exit exit;

	@BeforeEach
	public void before() {
		// Create controller
		controller = new MovementController();

		// Add exit
		exit = Exit.of(Direction.EAST, loc);

		// Add requirement
		final Requirement req = mock(Requirement.class);
		when(req.result(actor, exit)).thenReturn(new Result(null, 42));
		controller.add(req);

		// Init movement mode
		final Transaction tx = actor.model().values().transaction(EntityValue.STAMINA, 42, "message");
		final MovementMode mode = mock(MovementMode.class);
		when(actor.movement()).thenReturn(mode);
		when(mode.transactions(42)).thenReturn(List.of(tx));
	}

	private void init() {
		actor.model().values().get(EntityValue.STAMINA.key()).set(42);
	}

	@Test
	public void move() throws ActionException {
		init();
		assertEquals(List.of(new Result(null, 42)), controller.move(actor, exit, 1));
		verify(actor.movement()).move(exit);
		assertEquals(0, actor.model().values().get(EntityValue.STAMINA.key()).get());
	}

	@Test
	public void moveListener() throws ActionException {
		final Listener listener = mock(Listener.class);
		controller.add(listener);
		init();
		controller.move(actor, exit, 1);
		verify(listener).update(actor, exit, loc);
	}

	@Test
	public void moveInsufficientStamina() throws ActionException {
		TestHelper.expect("message", () -> controller.move(actor, exit, 1));
	}

	@Test
	public void moveCannotMove() throws ActionException {
		final MovementMode mode = actor.movement();
		doThrow(ActionException.of("doh")).when(mode).move(exit);
		init();
		TestHelper.expect("doh", () -> controller.move(actor, exit, 1));
	}
}
