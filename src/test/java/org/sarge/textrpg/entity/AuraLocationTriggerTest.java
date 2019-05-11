package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.entity.Entity.LocationTrigger;
import org.sarge.textrpg.entity.EntityModel.AppliedEffect;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.world.Location;

public class AuraLocationTriggerTest extends ActionTestBase {
	private LocationTrigger trigger;
	private Effect effect;
	private EffectController controller;
	private AppliedEffect applied;

	@BeforeEach
	public void before() {
		effect = new Effect.Builder().name("name").modifier(EntityValue.ARMOUR.key()).size(Calculation.literal(42)).duration(DURATION).build();
		controller = mock(EffectController.class);
		applied = mock(AppliedEffect.class);
		when(controller.apply(effect, actor)).thenReturn(applied);
		trigger = new AuraLocationTrigger(effect, controller);
	}

	@Test
	public void trigger() {
		trigger.trigger(actor);
		verify(controller).apply(effect, actor);
		assertEquals(1, actor.manager().queue().size());
	}

	@Test
	public void refresh() {
		trigger.trigger(actor);
		actor.manager().queue().manager().advance(DURATION.toMillis());
		// TODO
	}

	@Test
	public void actorLeavesLocation() {
		trigger.trigger(actor);
		when(actor.location()).thenReturn(mock(Location.class));
		actor.manager().queue().manager().advance(DURATION.toMillis());
		verify(applied).remove();
		assertEquals(0, actor.manager().queue().size());
	}
}
