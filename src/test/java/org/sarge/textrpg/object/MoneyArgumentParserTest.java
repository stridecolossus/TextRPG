package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Coin;
import org.sarge.textrpg.common.NumericConverter;
import org.sarge.textrpg.parser.NumericArgumentParser;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class MoneyArgumentParserTest {
	private MoneyArgumentParser parser;

	@BeforeEach
	public void before() {
		final Coin coin = new Coin("coin");
		parser = new MoneyArgumentParser(new NumericArgumentParser(mock(NumericConverter.class)), List.of(coin));
	}

	@Test
	public void constructor() {
		assertEquals(2, parser.count());
	}

	@Test
	public void parse() {
		final NameStore store = mock(NameStore.class);
		when(store.matches("coin", "coin")).thenReturn(true);

		final WordCursor cursor = new WordCursor("42 coin", store, Set.of());
		assertEquals(new Money(42), parser.parse(cursor));
	}
}
