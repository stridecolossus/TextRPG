package org.sarge.textrpg.util;

import java.util.Arrays;
import java.util.List;

/**
 * Formats numerals to text and vice-versa.
 * @author Sarge
 */
public class EnglishNumericFormatter implements NumericFormatter {
	private static final List<String> TENS = Arrays.asList(new String[]{
		"twenty",
		"thirty",
		"forty",
		"fifty",
		"sixty",
		"seventy",
		"eighty",
		"ninety",
	});
	
	private static final List<String> NUMBERS = Arrays.asList(new String[]{
		"zero",
		"one",
		"two",
		"three",
		"four",
		"five",
		"six",
		"seven",
		"eight",
		"nine",
		"ten",
		"eleven",
		"twelve",
		"thirteen",
		"fourteen",
		"fifteen",
		"sixteen",
		"seventeen",
		"eighteen",
		"nineteen",
	});
	
	@Override
	public String format(int num) {
		if(num < 0) throw new IllegalArgumentException("Number cannot be negative");
		
		if(num < 20) {
			return NUMBERS.get(num);
		}
		else
		if(num < 100) {
			final StringBuilder builder = new StringBuilder();
			final int tens = num / 10;
			builder.append(TENS.get(tens - 2));
			final int ones = num % 10;
			if(ones > 0) {
				builder.append(' ');
				builder.append(NUMBERS.get(ones));
			}
			return builder.toString();
		}
		else
		if(num < 1000) {
			final StringBuilder builder = new StringBuilder();
			builder.append(NUMBERS.get(num / 100));
			builder.append(" hundred");
			if(num % 100 > 0) {
				builder.append(" and ");
				builder.append(format(num % 100));
			}
			return builder.toString();
		}
		else {
			throw new UnsupportedOperationException("Cannot format a number larger than one thousand");
		}
	}
	
	@Override
	public int format(String text) {
		final String[] tokens = text.trim().toLowerCase().replaceAll("[^a-z\\s]", " ").split("\\s+");
		return Arrays.stream(tokens).mapToInt(this::map).reduce(this::combine).orElse(0);
	}
	
	/**
	 * Maps a token to its integer value.
	 * @param token Numeric token
	 * @return Integer
	 */
	private int map(String token) {
		// Handle hundreds
		if(token.equals("hundred")) return 100;
		
		// Check for tens
		final int tens = TENS.indexOf(token);
		if(tens >= 0) return (tens + 2) * 10;
		
		// Check for numbers
		final int numbers = NUMBERS.indexOf(token);
		if(numbers > 0) return numbers;
		
		// Otherwise ignore
		return 0;
	}

	/**
	 * Combiner.
	 * @param a Total so far
	 * @param b Next number
	 * @return Combined number
	 */
	private int combine(int a, int b) {
		if(b == 0) {
			// Ignore
			return a;
		}
		else
		if(b >= 100) {
			// Multiply hundreds
			return a * b;
		}
		else {
			// Otherwise sum
			return a + b;
		}
	}
}
