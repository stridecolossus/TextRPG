package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.textrpg.common.Emission.Type;
import org.sarge.textrpg.util.Percentile;

public class EmissionTest {
	@Test
	public void constructor() {
		final Emission sound = new Emission(Type.SOUND, Percentile.HALF);
		assertEquals(Type.SOUND, sound.type());
		assertEquals(Percentile.HALF, sound.intensity());
	}
}
