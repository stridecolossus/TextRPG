package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.DefaultNameStore;
import org.sarge.textrpg.util.NameStore;

public class MoneyFormatterTest {
	private MoneyFormatter formatter;
	private NameStore store;

	@BeforeEach
	public void before() {
		// Create names
		final var map = Map.of(
			"gold.single", "gold",
			"gold.multiple", "gold",
			"silver.single", "silver penny",
			"silver.multiple", "silver pennies",
			"copper.single", "copper",
			"copper.multiple", "coppers",
			"coin.and", "and"
		);
		store = new DefaultNameStore(map);

		// Create formatter
		final Coin copper = new Coin("copper");
		final Coin silver = new Coin("silver", 100, copper);
		final Coin gold = new Coin("gold", 20, silver);
		formatter = new MoneyFormatter(List.of(gold, silver, copper), ArgumentFormatter.PLAIN);
	}

	@ParameterizedTest(name="{0} => [{1}]")
	@MethodSource("createAmounts")
	public void format(int amount, String expected) {
		assertEquals(expected, formatter.format(amount, store));
	}

	static Stream<Arguments> createAmounts() {
		final Map<Integer, String> amounts = Map.of(
			2000, "1 gold",
			2018, "1 gold and 18 coppers",
			2100, "1 gold and 1 silver penny",
			2301, "1 gold, 3 silver pennies and 1 copper",
			300,  "3 silver pennies",
			321,  "3 silver pennies and 21 coppers"
		);
		return amounts.entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
	}
}
