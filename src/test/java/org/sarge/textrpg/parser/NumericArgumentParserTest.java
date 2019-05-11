package org.sarge.textrpg.parser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.NumericConverter;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class NumericArgumentParserTest {
	private NumericArgumentParser parser;
	private NumericConverter converter;

	@BeforeEach
	public void before() {
		converter = mock(NumericConverter.class);
		parser = new NumericArgumentParser(converter);
	}

	@Test
	public void parseNumeric() {
		final WordCursor cursor = new WordCursor("42", mock(NameStore.class), Set.of());
		parser.parse(cursor);
		verifyZeroInteractions(converter);
	}

	@Test
	public void parseIntegerToken() {
		final WordCursor cursor = new WordCursor("token", mock(NameStore.class), Set.of());
		parser.parse(cursor);
		verify(converter).convert("token");
	}
}
