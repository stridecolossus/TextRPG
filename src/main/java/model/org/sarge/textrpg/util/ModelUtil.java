package org.sarge.textrpg.util;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.ActionException;

/**
 * Model utilities.
 * @author Sarge
 */
public final class ModelUtil {
	private ModelUtil() {
		// Utility class
	}

	/**
	 * Creates a converter for an enumeration that has a short-name.
	 * @param clazz		Enumeration class
	 * @param mapper	Short-name mapper
	 * @return Enumeration converter
	 */
	public static <E extends Enum<E>> Converter<E> converter(Class<E> clazz, Function<E, String> mapper) {
		// Build short-name table
		final Map<String, E> table = Arrays.stream(clazz.getEnumConstants()).collect(toMap(mapper, Function.identity()));
		
		// Create converter
		final Converter<E> converter = Converter.enumeration(clazz);
		return new Converter<E>() {
			@Override
			public E convert(String str) {
				final E value = table.get(str);
				if(value == null) {
					return converter.convert(str);
				}
				else {
					return value;
				}
			}
		};
	}
	
	/**
	 * Consumer that throws an {@link ActionException}.
	 * @param <T>
	 */
	public interface ExceptionConsumer<T> {
		void accept(T arg) throws ActionException;
	}

	/**
	 * Wraps a consumer that can throw a checked exception and converts to runtime.
	 * @param consumer Consumer
	 * @return Wrapped consumer
	 */
	public static <T> Consumer<T> wrap(ExceptionConsumer<T> consumer) {
		return arg -> {
			try {
				consumer.accept(arg);
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		};
	}
}
