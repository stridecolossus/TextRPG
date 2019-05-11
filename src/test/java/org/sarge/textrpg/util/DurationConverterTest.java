package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

public class DurationConverterTest {
	private DurationConverter converter;

	@BeforeEach
	public void before() {
		converter = DurationConverter.CONVERTER;
	}

	@Test
	public void iso() {
		final String str = "P2DT3H4M";
		final Duration duration = converter.apply(str);
		assertEquals(Duration.parse(str), duration);
	}

	@ParameterizedTest
	@CsvSource({"PT2H, 2h", "PT25M, 25m", "PT90S, 90s", "PT0.500S, 500ms"})
	public void custom(@ConvertWith(DurationArgumentConverter.class) Duration expected, String input) {
		assertEquals(expected, converter.apply(input));
	}

	@Test
	public void customEmptyNumber() {
		assertThrows(NumberFormatException.class, () -> converter.apply("ms"));
	}

	@Test
	public void customInvalidNumber() {
		assertThrows(NumberFormatException.class, () -> converter.apply("0d"));
		assertThrows(NumberFormatException.class, () -> converter.apply("-1m"));
	}

	@Test
	public void customInvalidUnit() {
		assertThrows(NumberFormatException.class, () -> converter.apply("42?"));
	}

	@Test
	public void check() {
		assertThrows(IllegalArgumentException.class, () -> DurationConverter.oneOrMore(Duration.ZERO));
		assertThrows(IllegalArgumentException.class, () -> DurationConverter.oneOrMore(Duration.ofMillis(-1)));
	}
}
