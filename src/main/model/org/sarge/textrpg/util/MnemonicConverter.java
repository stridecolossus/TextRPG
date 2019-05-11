package org.sarge.textrpg.util;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import org.sarge.lib.util.Converter;

/**
 * Converter for a enumeration with an additional lookup table, e.g. for enumerations with a mnenomic or short-name.
 * @author Sarge
 */
public final class MnemonicConverter {
	private MnemonicConverter() {
	}

	/**
	 * Creates a converter with an additional name lookup table.
	 * @param clazz			Enumeration class
	 * @param mapper		Maps enumeration constants to names
	 * @return Converter
	 */
	public static <E extends Enum<E>> Converter<E> converter(Class<E> clazz, Function<E, String> mapper) {
		// Build name table
		final Function<E, String> lowercase = mapper.andThen(String::toLowerCase);
		final Map<String, E> table = Arrays.stream(clazz.getEnumConstants()).collect(toMap(lowercase, Function.identity()));

		// Create fall-back converter
		final Converter<E> converter = Converter.enumeration(clazz);

		// Create combined converter
		return str -> {
			// Lookup by name
			final E value = table.get(str.toLowerCase());

			// Otherwise use converter
			if(value == null) {
				return converter.apply(str);
			}
			else {
				return value;
			}
		};
	}
}
