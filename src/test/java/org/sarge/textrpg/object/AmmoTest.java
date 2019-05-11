package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Calculation;

public class AmmoTest {
	private Ammo ammo;
	private Damage damage;

	@BeforeEach
	public void before() {
		damage = new Damage(Damage.Type.PIERCING, Calculation.ZERO, Effect.NONE);
		ammo = new Ammo(new Ammo.Descriptor(ObjectDescriptor.of("ammo"), Ammo.Type.ARROW, damage, Percentile.ONE), 3);
	}

	@Test
	public void descriptor() {
		assertEquals("ammo", ammo.name());
		assertNotNull(ammo.descriptor());
		assertEquals(Ammo.Type.ARROW, ammo.descriptor().type());
		assertEquals(Percentile.ONE, ammo.descriptor().durability());
		assertEquals(true, ammo.isCategory("ARROW"));
		assertEquals(false, ammo.isCategory("BOLT"));
		assertEquals(false, ammo.isCategory("STONE"));
	}
}
