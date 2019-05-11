package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class MoveActionTest extends ActionTestBase {
	private MoveAction action;
	private MovementController mover;
	private Exit exit;

	@BeforeEach
	public void before() throws ActionException {
		// Create link
		final Link link = mock(Link.class);
		when(link.route()).thenReturn(Route.NONE);
		when(link.isTraversable()).thenReturn(true);
		when(link.controller()).thenReturn(Optional.of(mock(Thing.class)));

		// Create an exit
		exit = new Exit(Direction.EAST, link, loc);
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		when(actor.perceives(link.controller().get())).thenReturn(true);

		// Create movement controller
		mover = mock(MovementController.class);

		// Create action
		action = new MoveAction(mover);
	}

	@Test
	public void move() throws ActionException {
		when(mover.move(actor, exit, 1)).thenReturn(List.of(new MovementController.Result(Description.of("message"), 0)));
		final Response response = action.move(actor, Direction.EAST);
		final Response expected = new Response.Builder().display().add("message").build();
		assertEquals(expected, response);
		verify(mover).move(actor, exit, 1);
	}

	@Test
	public void moveInvalidDirection() throws ActionException {
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		TestHelper.expect("move.invalid.direction", () -> action.move(actor, Direction.EAST));
	}

	@Test
	public void moveHiddenLink() throws ActionException {
		when(actor.perceives(any())).thenReturn(false);
		TestHelper.expect("move.invalid.direction", () -> action.move(actor, Direction.EAST));
	}

	@Test
	public void moveCannotTraverse() throws ActionException {
		when(exit.link().reason(actor)).thenReturn(Optional.of(new Description("reason")));
		TestHelper.expect("reason", () -> action.move(actor, Direction.EAST));
	}

	@Test
	public void moveTraversalMessage() throws ActionException {
		when(exit.link().message()).thenReturn(Optional.of(Description.of("message")));
		final Response response = action.move(actor, Direction.EAST);
		final Response expected = new Response.Builder().display().add("message").build();
		assertEquals(expected, response);
	}
}
