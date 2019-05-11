package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.Calculation;

public class WeaponTest {
	private Weapon weapon;

	@BeforeEach
	public void before() {
		final Skill skill = Skill.NONE;
		final Damage damage = new Damage(Damage.Type.SLASHING, Calculation.ZERO, Effect.NONE);
		final var descriptor = new ObjectDescriptor.Builder("weapon").slot(Slot.MAIN).build();
		weapon = new Weapon(new Weapon.Descriptor(new DurableObject.Descriptor(descriptor, 1), skill, 2, damage, null, "enemy"));
	}

	@Test
	public void constructor() {
		assertEquals("weapon", weapon.name());
	}

	@Test
	public void descriptor() {
		final Weapon.Descriptor descriptor = weapon.descriptor();
		assertNotNull(descriptor);
		assertNotNull(descriptor.skill());
		assertEquals(2, descriptor.speed());
		assertNotNull(descriptor.damage());
		assertEquals(Optional.empty(), descriptor.ammo());
		assertEquals(Optional.of("enemy"), descriptor.enemy());
	}
}
