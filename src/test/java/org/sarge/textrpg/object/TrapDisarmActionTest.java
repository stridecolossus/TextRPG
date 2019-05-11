package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Trap;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.util.TextHelper;

public class TrapDisarmActionTest extends ActionTestBase {
	private TrapDisarmAction action;
	private Openable openable;
	private Trap trap;

	@BeforeEach
	public void before() {
		// Create known trap
		trap = new Trap(Effect.NONE, Percentile.HALF);
		when(actor.hidden().contains(trap)).thenReturn(true);

		// Create trapped openable
		final var model = new Openable.Model(new Openable.Lock(ObjectDescriptor.of("key"), Percentile.ONE, trap));
		openable = () -> model;

		// Create skill
		action = new TrapDisarmAction(skill);
	}

	@Test
	public void disarm() throws ActionException {
		addRequiredSkill();
		check(true);
	}

	@Test
	public void disarmNotKnown() throws ActionException {
		addRequiredSkill();
		when(actor.hidden().contains(trap)).thenReturn(false);
		TestHelper.expect("disarm.unknown.trap", () -> action.disarm(actor, openable));
	}

	@Test
	public void disarmAlreadyDisarmed() throws ActionException {
		openable.model().disarm();
		addRequiredSkill();
		TestHelper.expect("disarm.already.disarmed", () -> action.disarm(actor, openable));
	}

	@Test
	public void disarmFailed() throws ActionException {
		check(false);
	}

	private void check(boolean detected) throws ActionException {
		// Start disarming
		final Response response = action.disarm(actor, openable);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete induction and check result
		final Response result = complete(response);
		final String expected = TextHelper.join("trap.disarm", detected ? "success" : "failed");
		assertEquals(Response.of(expected), result);
	}
}
