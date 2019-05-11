package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class StringArgumentParserTest {
	private StringArgumentParser parser;
	private NameStore store;

	@BeforeEach
	public void before() {
		parser = new StringArgumentParser("key");
		store = mock(NameStore.class);
		when(store.matches("key", "literal")).thenReturn(true);
	}

	@Test
	public void parse() {
		final WordCursor cursor = new WordCursor("literal", store, Set.of());
		assertEquals("key", parser.parse(cursor));
	}

	@Test
	public void parseFailed() {
		final WordCursor cursor = new WordCursor("cobblers", store, Set.of());
		assertEquals(null, parser.parse(cursor));
	}
}
