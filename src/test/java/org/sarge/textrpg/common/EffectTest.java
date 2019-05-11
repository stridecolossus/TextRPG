package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.util.Calculation;

public class EffectTest {
	private Effect effect;

	@BeforeEach
	public void before() {
		effect = new Effect.Builder()
			.name("name")
			.modifier(EntityValue.ARMOUR.key())
			.size(Calculation.literal(1))
			.duration(Duration.ofSeconds(2))
			.times(3)
			.build();
	}

	@Test
	public void constructor() {
		assertEquals("name", effect.name());
		assertEquals(EntityValue.ARMOUR.key(), effect.modifier());
		assertNotNull(effect.size());
		assertEquals(Effect.Group.DEFAULT, effect.group());
		assertEquals(Duration.ofSeconds(2), effect.duration());
		assertEquals(3, effect.times());
	}

	@Test
	public void invalidRepeatingEffect() {
		final var builder = new Effect.Builder().name("name").modifier(EntityValue.ARMOUR.key()).times(2);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
}
