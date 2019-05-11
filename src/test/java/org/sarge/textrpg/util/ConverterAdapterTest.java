package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.sarge.lib.util.Converter;

public class ConverterAdapterTest {
	@Test
	public void convert() {
		final Converter<Integer> converter = new ConverterAdapter<>(Map.of("key", 42), Converter.INTEGER);
		assertEquals(Integer.valueOf(42), converter.apply("key"));
		assertEquals(Integer.valueOf(42), converter.apply("42"));
		assertThrows(NumberFormatException.class, () -> converter.apply("cobblers"));
	}
}
