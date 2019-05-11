package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;
import org.sarge.textrpg.world.Direction;

public class EnumArgumentParserTest {
	private EnumArgumentParser<Direction> parser;
	private NameStore store;

	@BeforeEach
	public void before() {
		parser = new EnumArgumentParser<>("direction", Direction.class);
		store = mock(NameStore.class);
		when(store.matches("direction.east", "e")).thenReturn(true);
	}

	@Test
	public void parse() {
		final WordCursor cursor = new WordCursor("e", store, Set.of());
		assertEquals(Direction.EAST, parser.parse(cursor));
	}

	@Test
	public void parseFailed() {
		final WordCursor cursor = new WordCursor("cobblers", store, Set.of());
		assertEquals(null, parser.parse(cursor));
	}
}
