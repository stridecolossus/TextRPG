package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.object.Weapon.Crossbow;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class CrossbowTest {
	private Crossbow crossbow;
	private Ammo bolt;

	@BeforeEach
	public void before() {
		final Damage damage = new Damage(Damage.Type.CRUSHING, Calculation.ZERO, Effect.NONE);
		final var descriptor = new ObjectDescriptor.Builder("weapon").slot(Slot.MAIN).build();
		crossbow = (Crossbow) new Weapon.Descriptor(new DurableObject.Descriptor(descriptor, 1), Skill.NONE, 2, damage, Ammo.Type.BOLT, null).create();
		bolt = ammo(Ammo.Type.BOLT);
	}

	private static Ammo ammo(Ammo.Type type) {
		final Damage damage = new Damage(Damage.Type.CRUSHING, Calculation.ZERO, Effect.NONE);
		return new Ammo(new Ammo.Descriptor(ObjectDescriptor.of("bolt"), type, damage, Percentile.ONE), 1);
	}

	@Test
	public void constructor() {
		assertEquals(false, crossbow.isLoaded());
	}

	@Test
	public void load() throws ActionException {
		crossbow.load(bolt);
		assertEquals(true, crossbow.isLoaded());
	}

	@Test
	public void loadAlreadyLoaded() throws ActionException {
		crossbow.load(bolt);
		TestHelper.expect("crossbow.already.loaded", () -> crossbow.load(bolt));
	}

	@Test
	public void loadInvalidAmmo() throws ActionException {
		final var arrow = ammo(Ammo.Type.ARROW);
		assertThrows(IllegalArgumentException.class, () -> crossbow.load(arrow));
	}

	@Test
	public void unload() throws ActionException {
		crossbow.load(bolt);
		crossbow.unload();
		assertEquals(false, crossbow.isLoaded());
	}

	@Test
	public void unloadNotLoaded() throws ActionException {
		TestHelper.expect("crossbow.not.loaded", () -> crossbow.unload());
	}
}
