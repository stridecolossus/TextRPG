package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Emission;

public class MaterialTest {
	@Test
	public void material() {
		final Material mat = new Material("glass", Collections.singleton(Emission.Type.LIGHT), Collections.singleton(DamageType.COLD), 42);
		assertEquals("glass", mat.name());
		assertEquals(true, mat.isTransparentTo(Emission.Type.LIGHT));
		assertEquals(true, mat.isDamagedBy(DamageType.COLD));
		assertEquals(42, mat.strength());
	}

	@Test
	public void defaultMaterial() {
		assertEquals("default", Material.DEFAULT.name());
		assertEquals(false, Material.DEFAULT.isTransparentTo(Emission.Type.LIGHT));
		assertEquals(false, Material.DEFAULT.isDamagedBy(DamageType.COLD));
		assertEquals(0, Material.DEFAULT.strength());
	}

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void invalidMaterial() {
		new Material("invalid", Collections.emptySet(), Collections.emptySet(), 42);
	}
}
