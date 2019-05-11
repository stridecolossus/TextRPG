package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class LiteralArgumentParserTest {
	private LiteralArgumentParser<CommandArgument> parser;
	private NameStore store;
	private CommandArgument literal;

	@BeforeEach
	public void before() {
		// Create literal command argument
		literal = mock(CommandArgument.class);
		when(literal.name()).thenReturn("key");

		// Create store
		store = mock(NameStore.class);
		when(store.matches("key", "literal")).thenReturn(true);

		// Create parser
		parser = new LiteralArgumentParser<>(literal);
	}

	@Test
	public void parse() {
		final WordCursor cursor = new WordCursor("literal", store, Set.of());
		assertEquals(literal, parser.parse(cursor));
	}

	@Test
	public void parseFailed() {
		final WordCursor cursor = new WordCursor("cobblers", store, Set.of());
		assertEquals(null, parser.parse(cursor));
	}
}
