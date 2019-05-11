package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;

public class MoneyTest {
	private Money money;

	@BeforeEach
	public void before() {
		money = new Money(42);
	}

	@Test
	public void constructor() {
		assertEquals("money", money.name());
		assertEquals(42, money.value());
		assertEquals(42, money.weight());
		assertEquals(Size.NONE, money.size());
		assertEquals(Percentile.ONE, money.visibility());
	}

	@Test
	public void describe() {
		final ArgumentFormatter.Registry formatters = new ArgumentFormatter.Registry();
		formatters.add(ArgumentFormatter.MONEY, ArgumentFormatter.PLAIN);
		final Description.Builder builder = new Description.Builder("money");
		money.describe(false, builder, formatters);
		assertNotNull(builder.get("amount"));
		assertEquals(42, builder.get("amount").argument());
	}
}
