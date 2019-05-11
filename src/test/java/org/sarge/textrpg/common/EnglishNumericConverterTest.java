package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class EnglishNumericConverterTest {
	private NumericConverter converter;

	@BeforeEach
	public void before() {
		converter = new EnglishNumericConverter();
	}

	@Test
	public void convertNegative() {
		assertThrows(IllegalArgumentException.class, () -> converter.convert(-1));
	}

	@Test
	public void convertZero() {
		assertEquals("zero", converter.convert(0));
	}

	@Test
	public void convertUnits() {
		assertEquals("one", converter.convert(1));
		assertEquals("two", converter.convert(2));
		assertEquals("three", converter.convert(3));
		assertEquals("four", converter.convert(4));
		assertEquals("five", converter.convert(5));
		assertEquals("six", converter.convert(6));
		assertEquals("seven", converter.convert(7));
		assertEquals("eight", converter.convert(8));
		assertEquals("nine", converter.convert(9));
	}

	@Test
	public void convertTeens() {
		assertEquals("eleven", converter.convert(11));
		assertEquals("twelve", converter.convert(12));
		assertEquals("thirteen", converter.convert(13));
		assertEquals("fourteen", converter.convert(14));
		assertEquals("fifteen", converter.convert(15));
		assertEquals("sixteen", converter.convert(16));
		assertEquals("seventeen", converter.convert(17));
		assertEquals("eighteen", converter.convert(18));
		assertEquals("nineteen", converter.convert(19));
	}

	@Test
	public void convertTens() {
		assertEquals("ten", converter.convert(10));
		assertEquals("twenty", converter.convert(20));
		assertEquals("thirty", converter.convert(30));
		assertEquals("forty", converter.convert(40));
		assertEquals("fifty", converter.convert(50));
		assertEquals("sixty", converter.convert(60));
		assertEquals("seventy", converter.convert(70));
		assertEquals("eighty", converter.convert(80));
		assertEquals("ninety", converter.convert(90));
	}

	@Test
	public void convertTensUnits() {
		assertEquals("twenty-one", converter.convert(21));
		assertEquals("thirty-two", converter.convert(32));
		assertEquals("forty-three", converter.convert(43));
		assertEquals("fifty-four", converter.convert(54));
		assertEquals("sixty-five", converter.convert(65));
		assertEquals("seventy-six", converter.convert(76));
		assertEquals("eighty-seven", converter.convert(87));
		assertEquals("ninety-eight", converter.convert(98));
	}

	@Test
	public void convertHundreds() {
		assertEquals("one hundred", converter.convert(100));
		assertEquals("two hundred", converter.convert(200));
		assertEquals("three hundred", converter.convert(300));
		assertEquals("four hundred", converter.convert(400));
		assertEquals("five hundred", converter.convert(500));
		assertEquals("six hundred", converter.convert(600));
		assertEquals("seven hundred", converter.convert(700));
		assertEquals("eight hundred", converter.convert(800));
		assertEquals("nine hundred", converter.convert(900));
	}

	@Test
	public void convertHundredsTensUnits() {
		assertEquals("one hundred and twenty-three", converter.convert(123));
		assertEquals("nine hundred and ninety-nine", converter.convert(999));
	}

	@Test
	public void convertInvalid() {
		assertThrows(UnsupportedOperationException.class, () -> converter.convert(1000));
	}

	@ParameterizedTest(name="First {0}")
	@ValueSource(ints={1, 21, 31})
	public void suffixFirst(int day) {
		assertEquals("st", converter.suffix(day));
	}

	@ParameterizedTest(name="Second {0}")
	@ValueSource(ints={2, 22})
	public void suffixSecond(int day) {
		assertEquals("nd", converter.suffix(day));
	}

	@ParameterizedTest(name="Third {0}")
	@ValueSource(ints={3, 23})
	public void suffixThird(int day) {
		assertEquals("rd", converter.suffix(day));
	}

	@ParameterizedTest
	@MethodSource("days")
	public void suffixDefault(int day) {
		switch(day) {
		case 1: case 21: case 31:
		case 2: case 22:
		case 3: case 23:
			break;

		default:
			assertEquals("th", converter.suffix(day));
		}
	}

	private static IntStream days() {
		return IntStream.range(1, 31);
	}
}
