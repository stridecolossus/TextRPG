package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.textrpg.entity.Effect;

public class LiquidTest {
	@Test
	public void constructor() {
		final Liquid liquid = new Liquid("liquid", 5, Effect.NONE);
		assertEquals("liquid", liquid.name());
		assertEquals(5, liquid.alcohol());
		assertEquals(true, liquid.isDrinkable());
		assertEquals(Effect.NONE, liquid.effects());
	}

	@Test
	public void water() {
		assertEquals("water", Liquid.WATER.name());
		assertEquals(true, Liquid.WATER.isDrinkable());
		assertEquals(Effect.NONE, Liquid.WATER.effects());
	}

	@Test
	public void oil() {
		assertEquals("oil", Liquid.OIL.name());
		assertEquals(false, Liquid.OIL.isDrinkable());
		assertEquals(Effect.NONE, Liquid.OIL.effects());
	}
}
