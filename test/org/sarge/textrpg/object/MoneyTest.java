package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Description;

public class MoneyTest {
	private Money money;
	
	@Before
	public void before() {
		money = new Money(42);
	}
	
	@Test
	public void constructor() {
		assertEquals(42, money.getValue());
		assertEquals(42, money.getWeight());
	}
	
	@Test
	public void describe() {
		final Description desc = money.describe();
		assertEquals("{money}", desc.get("name"));
		assertEquals("42", desc.get("value"));
	}
}
