package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CoinTest {
	@Test
	public void coin() {
		final Coin copper = new Coin("copper");
		final Coin silver = new Coin("silver", 100, copper);
		final Coin gold = new Coin("gold", 20, silver);
		assertEquals("copper", copper.name());
		assertEquals(1, copper.value());
		assertEquals(100, silver.value());
		assertEquals(2000, gold.value());
	}
}
