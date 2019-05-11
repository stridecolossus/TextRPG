package org.sarge.textrpg.util;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Text utilities.
 * @author Sarge
 */
public final class TextHelper {
	private static final Map<Class<?>, String> PREFIX = new HashMap<>();

	private TextHelper() {
	}

	/**
	 * Builds a full-stop delimited description key from the given key-parts.
	 * @param keys Key(s)
	 * @return Description key
	 */
	public static String join(Object... keys) {
		return Arrays.stream(keys).map(Object::toString).collect(joining(".")).toLowerCase();
	}

	/**
	 * Builds the prefixed name for the given enumeration value.
	 * The prefix is the <i>first</i> word (lower-case) of the enumeration class-name, e.g. <tt>damage</tt> for <tt>DamageType</tt>.
	 * @param e Enumeration constant
	 * @return Prefixed name
	 */
	public static <E extends Enum<E>> String prefix(E e) {
		final String prefix = PREFIX.computeIfAbsent(e.getClass(), TextHelper::prefix);
		return join(prefix, e.name());
	}

	/**
	 * Builds the prefix for an enumeration.
	 * @param clazz Enumeration class
	 * @return Prefix
	 */
	private static String prefix(Class<?> clazz) {
		final String tokens[] = StringUtils.splitByCharacterTypeCamelCase(clazz.getSimpleName());
		return tokens[0].toLowerCase();
	}

	/**
	 * Wraps a string with the given character.
	 * @param str 		String to wrap
	 * @param ch		Wrapping character
	 * @return Wrapped string
	 */
	public static String wrap(String str, char ch) {
		return wrap(str, ch, ch);
	}

	/**
	 * Wraps a string with the given characters.
	 * @param str		String to wrap
	 * @param left		Left-hand character
	 * @param right		Right-hand character
	 * @return Wrapped string
	 */
	public static String wrap(String str, char left, char right) {
		final StringBuilder sb = new StringBuilder();
		sb.append(left);
		sb.append(str);
		sb.append(right);
		return sb.toString();
	}
}
