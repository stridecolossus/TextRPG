package org.sarge.textrpg.common;

import org.springframework.stereotype.Component;

/**
 * English numeric converter.
 * @author Sarge
 */
@Component
public class EnglishNumericConverter implements NumericConverter {
	/**
	 * Units and teens.
	 */
	private static final String[] TEENS = {
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
	};

	/**
	 * Tens.
	 */
	private static final String[] TENS = {
		"twenty",
		"thirty",
		"forty",
		"fifty",
		"sixty",
		"seventy",
		"eighty",
		"ninety",
	};

	@Override
	public int convert(String token) {
		// TODO
		// - tokenize space/hyphen
		// - recursively parse somehow ~ digit index
		// - build in reverse, e.g. 234 = two hundred and thirty four => four -> 4, thirty -> 30, two hundred and -> 2 (remove literals)
		return 0;
	}

	@Override
	public String convert(int value) {
		// Check positive
		if(value < 0) {
			throw new IllegalArgumentException("Cannot convert a negative number");
		}

		// Handle units and teens
		if(value < 20) {
			return TEENS[value];
		}

		// Handle tens
		if(value < 100) {
			final StringBuilder builder = new StringBuilder();
			final int tens = value / 10;
			builder.append(TENS[tens - 2]);
			final int remainder = value - tens * 10;
			if(remainder > 0) {
				builder.append('-');
				builder.append(TEENS[remainder]);
			}
			return builder.toString();
		}

		// Handle hundreds
		if(value < 1000) {
			final StringBuilder builder = new StringBuilder();
			final int hundreds = value / 100;
			builder.append(TEENS[hundreds]);
			builder.append(" hundred");
			final int remainder = value - hundreds * 100;
			if(remainder > 0) {
				builder.append(" and ");
				builder.append(convert(remainder));
			}
			return builder.toString();
		}

		throw new UnsupportedOperationException("Cannot convert value larger than a thousand: " + value);
	}

	@Override
	public String suffix(int day) {
		switch(day) {
		case 1: case 21: case 31: return "st";
		case 2:	case 22: return "nd";
		case 3: case 23: return "rd";
		default: return "th";
		}
	}
}
