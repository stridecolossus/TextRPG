package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.EffectController;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;

public class FleeActionTest extends ActionTestBase {
	private FleeAction action;
	private MovementController mover;
	private Exit exit;

	@BeforeEach
	public void before() {
		// Create exit
		final Link link = mock(Link.class);
		when(link.route()).thenReturn(Route.NONE);
		exit = new Exit(Direction.EAST, link, loc);
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		when(link.isTraversable()).thenReturn(true);

		// Create action
		mover = mock(MovementController.class);
		action = new FleeAction(mover, mock(EffectController.class));
	}

	@Test
	public void flee() throws ActionException {
		when(actor.manager().induction().isActive()).thenReturn(true);
		final Response expected = new Response.Builder().add(new Description("action.flee.message")).display().build();
		assertEquals(expected, action.flee(actor));
		verify(actor.manager().induction()).interrupt();
	}

	@Test
	public void fleeEmptyExits() throws ActionException {
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		assertEquals(Response.of("flee.cannot.flee"), action.flee(actor));
		// TODO - check panic applied
	}

	@Test
	public void fleeCannotTraverse() throws ActionException {
		when(exit.link().isTraversable()).thenReturn(false);
		assertEquals(Response.of("flee.cannot.flee"), action.flee(actor));
	}

	@Test
	public void fleeNotPerceived() throws ActionException {
		when(exit.link().controller()).thenReturn(Optional.of(mock(Thing.class)));
		assertEquals(Response.of("flee.cannot.flee"), action.flee(actor));
	}

	@Test
	public void fleeMoveFails() throws ActionException {
		when(mover.move(actor, exit, 1)).thenThrow(ActionException.of("doh"));
		assertEquals(Response.of("doh"), action.flee(actor));
	}

	@Test
	public void fleeQueued() throws ActionException {
		// Register a primary induction
		final Induction.Manager induction = actor.manager().induction();
		when(induction.isPrimary()).thenReturn(true);

		// Start fleeing
		final Response response = action.flee(actor);
		assertNotNull(response);

		// Check fleeing induction
		final Induction.Descriptor result = response.induction().get().descriptor();
		assertEquals(true, result.isFlag(Induction.Flag.REPEATING));
	}
}
