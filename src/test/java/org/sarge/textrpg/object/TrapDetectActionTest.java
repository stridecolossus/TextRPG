package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

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
import org.sarge.textrpg.util.TextHelper;

public class TrapDetectActionTest extends ActionTestBase {
	private TrapDetectAction action;
	private Openable openable;
	private Trap trap;

	@BeforeEach
	public void before() {
		trap = new Trap(Effect.NONE, Percentile.HALF);
		final var model = new Openable.Model(new Openable.Lock(ObjectDescriptor.of("key"), Percentile.ZERO, trap));
		openable = () -> model;
		action = new TrapDetectAction(skill, DURATION);
	}

	@Test
	public void detect() throws ActionException {
		addRequiredSkill();
		check(true);
		verify(actor.hidden()).add(trap, DURATION);
	}

	@Test
	public void detectNotTrapped() throws ActionException {
		openable = () -> new Openable.Model(new Openable.Lock(ObjectDescriptor.of("key"), Percentile.ZERO, null));
		addRequiredSkill();
		check(false);
	}

	@Test
	public void detectFailed() throws ActionException {
		check(false);
	}

	private void check(boolean detected) throws ActionException {
		// Start detecting
		final Response response = action.detect(actor, openable);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete induction and check result
		final Response result = complete(response);
		final String expected = TextHelper.join("trap.detect", detected ? "success" : "failed");
		assertEquals(Response.of(expected), result);
	}
}
