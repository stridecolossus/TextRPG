package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;

import org.sarge.lib.util.AbstractObject;
import org.sarge.lib.util.Converter;

/**
 * Adapter that supports a lookup table of values before delegating to the underlying converter.
 * @author Sarge
 * @param <T> Data-type
 */
public class ConverterAdapter<T> extends AbstractObject implements Converter<T> {
	private final Map<String, T> table;
	private final Converter<T> converter;

	/**
	 * Constructor.
	 * @param table			Mapping table
	 * @param converter		Delegate converter
	 */
	public ConverterAdapter(Map<String, T> table, Converter<T> converter) {
		this.table = Map.copyOf(table);
		this.converter = notNull(converter);
	}

	@Override
	public T apply(String str) throws NumberFormatException {
		final T value = table.get(str);
		if(value == null) {
			return converter.apply(str);
		}
		else {
			return value;
		}
	}
}
