package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.sarge.lib.util.Converter;

public class MnemonicConverterTest {
	private static enum MockEnum {
		ONE,
		TWO
	}

	@Test
	public void converter() {
		// Create mnenomic mapping
		final Function<MockEnum, String> mapper = e -> {
			switch(e) {
			case ONE:	return "a";
			case TWO:	return "b";
			default:	return null;
			}
		};

		// Create converter
		final Converter<MockEnum> converter = MnemonicConverter.converter(MockEnum.class, mapper);
		assertNotNull(converter);

		// Check mnenomic conversion
		assertEquals(MockEnum.ONE, converter.apply("a"));
		assertEquals(MockEnum.TWO, converter.apply("b"));

		// Check fall-back conversion
		assertEquals(MockEnum.ONE, converter.apply("ONE"));

		// Check case-insensitive conversion
		assertEquals(MockEnum.ONE, converter.apply("A"));
		assertEquals(MockEnum.ONE, converter.apply("one"));
	}
}
