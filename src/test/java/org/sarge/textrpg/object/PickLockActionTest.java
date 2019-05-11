package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Trap;
import org.sarge.textrpg.entity.EffectController;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class PickLockActionTest extends ActionTestBase {
	private PickLockAction action;
	private Openable openable;
	private DurableObject lockpicks;

	@BeforeEach
	public void before() {
		final Openable.Model model = new Openable.Model(new Openable.Lock(ObjectDescriptor.of("key"), Percentile.HALF, null));
		openable = () -> model;
		lockpicks = mock(DurableObject.class);
		action = new PickLockAction(skill, mock(EffectController.class));
	}

	@Test
	public void pick() throws ActionException {
		// Start pick-lock
		final Response response = action.pick(actor, lockpicks, openable);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));
	}

	@Test
	public void pickLatch() throws ActionException {
		final Openable.Model latch = new Openable.Model(Openable.Lock.LATCH);
		openable = () -> latch;
		TestHelper.expect("pick.invalid.latch", () -> action.pick(actor, lockpicks, openable));
	}

	@Test
	public void pickNotLocked() throws ActionException {
		openable.model().apply(Openable.Operation.UNLOCK);
		TestHelper.expect("pick.not.locked", () -> action.pick(actor, lockpicks, openable));
	}

	@Test
	public void pickAlreadyUnlocked() throws ActionException {
		final Response response = action.pick(actor, lockpicks, openable);
		openable.model().apply(Openable.Operation.UNLOCK);
		TestHelper.expect("pick.already.unlocked", () -> response.induction().get().induction().complete());
	}

	@Test
	public void pickSuccess() throws ActionException {
		addRequiredSkill();
		final Response response = action.pick(actor, lockpicks, openable);
		final Response result = response.induction().get().induction().complete();
		assertEquals(Response.of("pick.success"), result);
	}

	@Test
	public void pickFailed() throws ActionException {
		final Response response = action.pick(actor, lockpicks, openable);
		final Response result = response.induction().get().induction().complete();
		assertEquals(Response.of("pick.failed"), result);
	}

	@Test
	public void pickTrapped() throws ActionException {
		final Trap trap = new Trap(Effect.NONE, Percentile.ONE);
		final Openable.Model trapped = new Openable.Model(new Openable.Lock(ObjectDescriptor.of("key"), Percentile.ONE, trap));
		final Response response = action.pick(actor, lockpicks, () -> trapped);
		final Response result = response.induction().get().induction().complete();
		assertEquals(Response.of("pick.trap.activated"), result);
		// TODO - effects
	}
}
