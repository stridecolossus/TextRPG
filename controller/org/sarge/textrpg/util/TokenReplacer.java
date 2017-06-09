package org.sarge.textrpg.util;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sarge.lib.util.Check;

/**
 * Token replacer utility.
 * @author Sarge
 */
public class TokenReplacer {
	private final Pattern pattern;
	private final Function<String, String> mapper;
	
	/**
	 * Constructor.
	 * @param pattern	Token pattern
	 * @param mapper	Maps token names to the replacement text
	 */
	public TokenReplacer(String pattern, Function<String, String> mapper) {
		Check.notNull(mapper);
		this.pattern = Pattern.compile(pattern);
		this.mapper = mapper;
	}
	
	/**
	 * Default constructor for bracket-delimited tokens.
	 */
	public TokenReplacer(Function<String, String> mapper) {
		this("\\{[\\w\\.\\/]+\\}", mapper);
	}

	/**
	 * Replaces tokens in a string with values from the argument mapper (null tokens are ignored).
	 * @param str 			String to token-replace
	 * @param recurse		Whether to recursively replace tokens
	 * @return String with tokens replaced
	 */
	public String replace(String str) {
		return replace(str, false);
	}
	
	/**
	 * Replaces tokens in a string with values from the argument mapper (null tokens are ignored).
	 * @param str 			String to token-replace
	 * @param recurse		Whether to recursively replace tokens
	 * @return String with tokens replaced
	 */
	public String replace(String str, boolean recurse) {
		// Iterate tokens
		final StringBuilder out = new StringBuilder(str);
		final Matcher matcher = pattern.matcher(out);
		int start = 0;
		while(matcher.find(start)) {
			// Find next token
			final String group = matcher.group();
			final String key = group.substring(1, group.length() - 1);
			
			// Lookup replacement text
			final String value = mapper.apply(key);

			// Replace token
			out.replace(matcher.start(), matcher.end(), value == null ? "" : value);
			
			// Continue from start of this match to handle recursive tokens
			start = matcher.start();
			if(!recurse && (value != null)) {
				start += value.length();
			}
		}

		// Strip excess spaces
		return out.toString();
	}
}
