package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Emission;

public class MaterialTest {
	private Material mat;

	@BeforeEach
	public void before() {
		mat = new Material.Builder("mat")
			.strength(42)
			.damaged(Damage.Type.FIRE)
			.transparent(Emission.LIGHT)
			.floats()
			.build();
	}

	@Test
	public void constructor() {
		assertEquals("mat", mat.name());
		assertEquals(42, mat.strength());
		assertEquals(true, mat.isDamagedBy(Damage.Type.FIRE));
		assertEquals(true, mat.isTransparentTo(Emission.LIGHT));
		assertEquals(true, mat.isFloating());
	}

	@Test
	public void defaultMaterial() {
		assertEquals("material.none", Material.NONE.name());
		assertEquals(0, Material.NONE.strength());
		assertEquals(false, Material.NONE.isDamagedBy(Damage.Type.FIRE));
	}

	@Test
	public void invalidStrength() {
		final Material.Builder builder = new Material.Builder("mat").strength(42);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	public void invalidZeroStrength() {
		final Material.Builder builder = new Material.Builder("mat").damaged(Damage.Type.FIRE);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
}
