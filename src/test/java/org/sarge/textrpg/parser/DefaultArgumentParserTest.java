package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class DefaultArgumentParserTest {
	private DefaultArgumentParser<?> parser;
	private CommandArgument arg;
	private WordCursor cursor;
	private NameStore store;

	@BeforeEach
	public void before() {
		// Create argument
		arg = mock(CommandArgument.class);

		// Create store
		store = mock(NameStore.class);
		when(store.matches("key", "arg")).thenReturn(true);

		// Create cursor
		cursor = new WordCursor("arg", store, Set.of());

		// Create parser
		parser = new DefaultArgumentParser<>(ignore -> Stream.of(arg), mock(Entity.class));
	}

	@Test
	public void parse() {
		when(arg.name()).thenReturn("key");
		assertEquals(arg, parser.parse(cursor));
	}

	@Test
	public void parseFailed() {
		when(arg.name()).thenReturn("cobblers");
		assertEquals(null, parser.parse(cursor));
	}
}
