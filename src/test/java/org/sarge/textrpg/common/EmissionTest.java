package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Emission.Type;
import org.sarge.textrpg.util.Percentile;

public class EmissionTest {
	@Test
	public void constructor() {
		final Emission sound = new Emission("sound", Type.SOUND, Percentile.HALF);
		assertEquals("sound", sound.getName());
		assertEquals(Type.SOUND, sound.getType());
		assertEquals(Percentile.HALF, sound.getIntensity());
	}
	
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void invalidName() {
		new Emission("invalid", Type.LIGHT, Percentile.HALF);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void missingName() {
		new Emission(null, Type.ODOUR, Percentile.HALF);
	}
	
	@Test
	public void hasName() {
		assertEquals(false, Type.LIGHT.hasName());
		assertEquals(true, Type.ODOUR.hasName());
		assertEquals(false, Type.SMOKE.hasName());
		assertEquals(true, Type.SOUND.hasName());
	}
	
	@Test
	public void light() {
		final Emission light = Emission.light(Percentile.HALF);
		assertEquals(null, light.getName());
		assertEquals(Type.LIGHT, light.getType());
		assertEquals(Percentile.HALF, light.getIntensity());
	}
}
