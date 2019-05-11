package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Induction.Flag;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.Percentile;

public class SwimmingControllerTest extends ActionTestBase {
	private SwimmingController controller;
	private EffectController effects;

	@BeforeEach
	public void before() {
		skill = new Skill.Builder().name("swimming").score(Percentile.ONE).defaultScore(Percentile.ZERO).build();
		effects = mock(EffectController.class);
		controller = new SwimmingController(effects, skill);
		actor.model().values().get(EntityValue.STAMINA.key()).set(1);
		actor.model().values().get(EntityValue.HEALTH.key()).set(1);
	}

	@Test
	public void start() {
		// Start swimming
		when(actor.isSwimEnabled()).thenReturn(true);
		controller.start(actor);
		verify(actor.model()).stance(Stance.SWIMMING);

		// Check induction started
		final ArgumentCaptor<Induction.Instance> captor = ArgumentCaptor.forClass(Induction.Instance.class);
		verify(actor.manager().induction()).start(captor.capture());

		// Check induction
		final Induction.Descriptor descriptor = captor.getValue().descriptor();
		assertEquals(true, descriptor.isFlag(Flag.PRIMARY));
		assertEquals(true, descriptor.isFlag(Flag.REPEATING));
	}

	@Test
	public void startSneaking() {
		actor.model().values().visibility().stance(Percentile.HALF);
		when(actor.model().stance()).thenReturn(Stance.SNEAKING);
		when(actor.isSwimEnabled()).thenReturn(true);
		controller.start(actor);
		assertEquals(Percentile.ONE, actor.model().values().visibility().get());
	}

	@Test
	public void startAlreadySwimming() {
		when(actor.model().stance()).thenReturn(Stance.SWIMMING);
		when(actor.isSwimEnabled()).thenReturn(true);
		assertThrows(IllegalStateException.class, () -> controller.start(actor));
	}

	@Test
	public void startCannotSwim() {
		assertThrows(IllegalStateException.class, () -> controller.start(actor));
	}

	@Test
	public void stop() {
		// Start swimming
		when(actor.isSwimEnabled()).thenReturn(true);
		controller.start(actor);
		when(actor.model().stance()).thenReturn(Stance.SWIMMING);

		// Stop swimming
		controller.stop(actor);
		verify(actor.model()).stance(Stance.DEFAULT);
		verify(actor.manager().induction()).stop();
	}

	@Test
	public void stopNotSwimming() {
		assertThrows(IllegalStateException.class, () -> controller.stop(actor));
	}

	@Test
	public void iterate() {
		addRequiredSkill();
		assertEquals(Response.EMPTY, controller.update(actor));
	}

	@Test
	public void iterateInsufficientStamina() {
		actor.model().values().get(EntityValue.STAMINA.key()).set(0);
		addRequiredSkill();
		assertEquals(Response.of("swim.drowning"), controller.update(actor));
		assertEquals(0, actor.model().values().get(EntityValue.HEALTH.key()).get());
		verify(effects).panic(actor, 1);
	}

	@Test
	public void iterateSkillFailure() {
		assertEquals(Response.of("swim.drowning"), controller.update(actor));
		assertEquals(0, actor.model().values().get(EntityValue.HEALTH.key()).get());
		verify(effects).panic(actor, 1);
	}
}
