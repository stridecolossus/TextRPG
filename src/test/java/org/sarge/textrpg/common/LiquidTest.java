package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LiquidTest {
	@Test
	public void constructor() {
		final Liquid liquid = new Liquid("liquid", Effect.NONE, Effect.Group.DISEASE);
		assertEquals("liquid", liquid.name());
		assertEquals(Effect.NONE, liquid.effect());
		assertEquals(Effect.Group.DISEASE, liquid.curative());
	}

	@Test
	public void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new Liquid("liquid", Effect.NONE, Effect.Group.DEFAULT));
	}

	@Test
	public void water() {
		assertEquals("water", Liquid.WATER.name());
		assertEquals(Effect.NONE, Liquid.WATER.effect());
	}

	@Test
	public void oil() {
		assertEquals("oil", Liquid.OIL.name());
		assertEquals(Effect.NONE, Liquid.OIL.effect());
	}
}
