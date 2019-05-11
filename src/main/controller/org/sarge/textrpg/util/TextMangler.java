package org.sarge.textrpg.util;

import static java.util.stream.Collectors.joining;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A <i>text mangler</i> is used to obfuscate text in other languages.
 * @author Sarge
 */
public class TextMangler {
	private static final String SPACE = " ";			// TODO - newlines? regex?
	private static final char DOT = '.';
	private static final Map<Integer, String> CACHE = new HashMap<>();

	private final Percentile score;

	/**
	 * Constructor.
	 * @param score Language score
	 * @throws IllegalArgumentException if the given score is zero or one
	 */
	public TextMangler(Percentile score) {
		if(Percentile.ZERO.equals(score) || Percentile.ONE.equals(score)) throw new IllegalArgumentException("Invalid text mangler score");
		this.score = notNull(score);
	}

	/**
	 * Mangles the given text.
	 * @param text Text
	 * @return Mangled text
	 */
	public String mangle(String text) {
		// Create word mangler
		final Function<String, String> mangler = word -> {
			if(Randomiser.isLessThan(score)) {
				return word;
			}
			else {
				final int len = word.length();
				return CACHE.computeIfAbsent(len, TextMangler::mangle);
			}
		};

		// Mangle text
		return Arrays.stream(text.split(SPACE)).map(mangler).collect(joining(SPACE));
	}

	/**
	 * @param len Word length
	 * @return Mangled word
	 */
	private static String mangle(int len) {
		final char[] dots = new char[len];
		Arrays.fill(dots, DOT);
		return new String(dots);
	}
}
