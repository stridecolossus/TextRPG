package org.sarge.textrpg.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.StreamUtil;

/**
 * Table of data indexed by an enumeration.
 * @author Sarge
 * @param <E> Enumeration
 */
public class DataTable<E extends Enum<E>> {
	private static final String WHITESPACE = "\\s+";

	/**
	 * Helper - Loads a data-table.
	 * @param r	Reader
	 * @return Rows
	 * @throws IOException if the table cannot be loaded
	 */
	public static Stream<String[]> load(Reader r) throws IOException {
		return new BufferedReader(r)
			.lines()
			.map(String::trim)
			.filter(line -> !line.startsWith("#"))
			.filter(StreamUtil.not(String::isEmpty))
			.map(line -> line.split(WHITESPACE));
	}

	/**
	 * Loads a data-table from the given input.
	 * @param clazz			Enumeration class
	 * @param r				Reader
	 * @param mandatory		Whether the resultant table <b>must</b> have a row for every enumeration constant
	 * @return Data-table
	 * @param <E> Enumeration
	 * @throws IOException if the table cannot be loaded
	 */
	public static <E extends Enum<E>> DataTable<E> load(Class<E> clazz, Reader r, boolean mandatory) throws IOException {
		// Parse table
		final Converter<E> converter = Converter.enumeration(clazz);
		final EnumMap<E, List<String>> map = new EnumMap<>(clazz);
		final List<String> cols;
		try(BufferedReader in = new BufferedReader(r)) {
			// Load table
			final var itr = load(in).iterator();
			if(!itr.hasNext()) throw new IOException("Empty table");

			// Load columns header
			cols = Arrays.asList(itr.next());

			// Parse table
			while(itr.hasNext()) {
				load(converter, itr.next(), map);
			}
		}

		// Check table integrity
		if(mandatory && (map.size() != clazz.getEnumConstants().length)) throw new IOException("Incomplete table");

		// Create data-table
		return new DataTable<>(cols, map);
	}

	/**
	 * Loads a table row.
	 * @param converter		Enumeration converter
	 * @param row			Table row
	 * @param table			Table
	 */
	private static <E extends Enum<E>> void load(Converter<E> converter, String[] row, Map<E, List<String>> table) {
		if(row.length < 2) throw new IllegalArgumentException("Incomplete line");
		final E key = converter.apply(row[0]);
		final List<String> list = Arrays.asList(row).subList(1, row.length);
		table.put(key, list);
	}

	private final List<String> cols;
	private final Map<E, List<String>> table;

	/**
	 * Constructor.
	 * @param cols		Column names
	 * @param table		Data-table
	 */
	public DataTable(List<String> cols, Map<E, List<String>> table) {
		if(cols.isEmpty()) throw new IllegalArgumentException("Empty list of columns");
		this.cols = List.copyOf(cols);
		this.table = new EnumMap<>(table);
		verify();
	}

	/**
	 * Checks all rows match the number of columns.
	 */
	private void verify() {
		final int expected = cols.size() - 1;
		if(!table.values().stream().allMatch(row -> row.size() == expected)) throw new IllegalArgumentException("Column-table size mis-match");
	}

	/**
	 * Extracts a column from this table.
	 * @param col			Column name
	 * @param converter		Data converter
	 * @return Column indexed by the enumeration
	 */
	public <T> Map<E, T> column(String col, Converter<T> converter) {
		// Lookup column
		final int index = cols.indexOf(col);
		if(index == -1) throw new IllegalArgumentException("Unknown column: " + col);
		if(index == 0) throw new IllegalArgumentException("Cannot extract the key column");

		// Extract and convert column
		final var map = new HashMap<E, T>();
		table.forEach((e, list) -> map.put(e, converter.apply(list.get(index - 1))));
		return new EnumMap<>(map);
	}


	/**
	 * Extracts a mapping function from this table.
	 * @param col			Column name
	 * @param converter		Data converter
	 * @return Mapping function for the given column
	 * @see #column(String, Converter)
	 */
	public <T> Function<E, T> function(String col, Converter<T> converter) {
		final Map<E, T> map = column(col, converter);
		return map::get;
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
