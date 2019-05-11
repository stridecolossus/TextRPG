package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class HandedFilterArgumentParserTest {
	private HandedFilterArgumentParser parser;
	private NameStore  store;

	@BeforeEach
	public void before() {
		parser = new HandedFilterArgumentParser(new IntegerArgumentParser());
		store = mock(NameStore.class);
		when(store.matches("handed", "handed")).thenReturn(true);
	}

	private void run(String line, ObjectDescriptor.Filter expected) {
		final WordCursor cursor = new WordCursor(line, store, Set.of());
		final ObjectDescriptor.Filter filter = parser.parse(cursor);
		assertEquals(expected, filter);
	}

	@Test
	public void parse() {
		run("1 handed", ObjectDescriptor.Filter.ONE_HANDED);
		run("2 handed", ObjectDescriptor.Filter.TWO_HANDED);
	}

	@Test
	public void parseFailed() {
		run("3 handed", null);
	}
}
