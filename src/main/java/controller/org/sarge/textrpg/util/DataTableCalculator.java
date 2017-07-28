package org.sarge.textrpg.util;

import java.util.Map;

import org.sarge.lib.util.ToString;

/**
 * Calculator for values derived from a {@link DataTable}.
 * @author Sarge
 */
public class DataTableCalculator {
	private final Map<?, Number>[] tables;

	/**
	 * Constructor.
	 * @param tables Data tables
	 */
	public DataTableCalculator(Map<?, Number>[] tables) {
		this.tables = tables.clone();
	}

	/**
	 * Multiplies the data-table entries for the given keys.
	 * @param keys Array of keys
	 * @return Multiplied value
	 * @throws IllegalArgumentException if the number of keys does not match the number of tables or a key is unknown
	 */
	public float multiply(Object... keys) {
		if(keys.length != tables.length) throw new IllegalArgumentException("Expected " + tables.length + " keys");
		float f = 1;
		for(int n = 0; n < keys.length; ++n) {
			final Number value = tables[n].get(keys[n]);
			if(value == null) throw new IllegalArgumentException("Unknown key: index=" + n);
			f = f * value.floatValue();
		}
		return f;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
