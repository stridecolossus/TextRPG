package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Emission;

public class MaterialTest {
	@Test
	public void material() {
		final Material mat = new Material("mat", Collections.singleton(Emission.Type.ODOUR), Collections.singleton(DamageType.COLD), 42);
		assertEquals("mat", mat.getName());
		assertEquals(true, mat.isTransparentTo(Emission.Type.ODOUR));
		assertEquals(true, mat.isDamagedBy(DamageType.COLD));
		assertEquals(42, mat.getStrength());
	}

	@Test
	public void defaultMaterial() {
		assertEquals("default", Material.DEFAULT.getName());
		assertEquals(false, Material.DEFAULT.isTransparentTo(Emission.Type.ODOUR));
		assertEquals(false, Material.DEFAULT.isDamagedBy(DamageType.COLD));
		assertEquals(0, Material.DEFAULT.getStrength());
	}
	
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void invalidMaterial() {
		new Material("invalid", Collections.emptySet(), Collections.emptySet(), 42);
	}
}
