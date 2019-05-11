package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class QualifiedArgumentParserTest {
	private QualifiedArgumentParser parser;
	private WorldObject obj;
	private NameStore store;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		// Create object parser
		final ArgumentParser<WorldObject> object = mock(ArgumentParser.class);
		obj = new ObjectDescriptor.Builder("object").qualifier("qualifier").build().create();
		when(object.parse(any())).thenReturn(obj);

		// Create name-store
		store = mock(NameStore.class);
		when(store.matches("object", "object")).thenReturn(true);
		when(store.matches("qualifier", "qualifier")).thenReturn(true);

		// Create parser
		parser = new QualifiedArgumentParser(object);
	}

	@Test
	public void parse() {
		final WordCursor cursor = new WordCursor("qualifier object", store, Set.of());
		assertEquals(obj, parser.parse(cursor));
	}

	@Test
	public void parseInvalidQualifier() {
		final WordCursor cursor = new WordCursor("cobblers object", store, Set.of());
		when(store.matches("cobblers", "cobblers")).thenReturn(true);
		assertEquals(null, parser.parse(cursor));
	}
}
