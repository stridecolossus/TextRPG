package org.sarge.textrpg.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.MapBuilder;
import org.sarge.lib.util.ToString;

/**
 * Adapter for a converter that can also use a lookup table.
 * @author Sarge
 * @param <T> Data-type
 */
public class TableConverter<T> implements Converter<T> {
	private final Converter<T> converter;
	private final Map<String, T> table;

	/**
	 * Constructor.
	 * @param converter		Converter
	 * @param table			Table
	 */
	public TableConverter(Converter<T> converter, Map<String, T> table) {
		Check.notNull(converter);
		Check.notEmpty(table);
		this.converter = converter;
		this.table = new HashMap<>(table);
	}

	/**
	 * Convenience constructor for a table with a single entry.
	 * @param converter		Converter
	 * @param key			Key
	 * @param value			Value
	 */
	public TableConverter(Converter<T> converter, String key, T value) {
		this(converter, Collections.singletonMap(key, value));
	}
	
	@Override
	public T convert(String str) {
		final T value = table.get(str);
		if(value == null) {
			return converter.convert(str);
		}
		else {
			return value;
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
	
	/**
	 * Creates a table-converter from the given input.
	 * @param in			Input
	 * @param converter		Converter
	 * @return Table converter
	 */
	public static <T> TableConverter<T> load(Reader in, Converter<T> converter) {
		// Load table
		final MapBuilder<String, T> builder = new MapBuilder<>();
		new BufferedReader(in).lines().forEach(line -> {
			// Tokenize line
			final String[] array = line.split(" ");
			if(array.length != 2) throw new IllegalArgumentException("Expected key-value");
			
			// Add table entry
			final String key = array[0];
			final T value = converter.convert(array[1]);
			builder.add(key, value);
		});

		// Create table converter
		return new TableConverter<>(converter, builder.build());
	}
}
