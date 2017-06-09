package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;
import org.sarge.textrpg.entity.Effect;

public class LiquidTest {
	@Test
	public void constructor() {
		final Liquid liquid = new Liquid("liquid", 5, Effect.NONE);
		assertEquals("liquid", liquid.getName());
		assertEquals(5, liquid.getAlcohol());
		assertEquals(true, liquid.isDrinkable());
		assertEquals(Optional.of(Effect.NONE), liquid.getEffect());
	}
	
	@Test
	public void water() {
		assertEquals("water", Liquid.WATER.getName());
		assertEquals(true, Liquid.WATER.isDrinkable());
		assertEquals(Optional.of(Effect.NONE), Liquid.WATER.getEffect());
	}
	
	@Test
	public void oil() {
		assertEquals("oil", Liquid.OIL.getName());
		assertEquals(false, Liquid.OIL.isDrinkable());
		assertEquals(Optional.empty(), Liquid.OIL.getEffect());
	}
}
