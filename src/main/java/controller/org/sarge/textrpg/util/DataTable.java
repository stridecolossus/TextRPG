package org.sarge.textrpg.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.sarge.lib.util.Converter;
import org.sarge.lib.util.StreamUtil;
import org.sarge.lib.util.Util;

/**
 * 2D table of string values.
 * @author Sarge
 * @param <E> Enumeration
 */
public class DataTable {
	private final List<String> cols;
	private final String[][] rows;

	/**
	 * Constructor.
	 * @param cols		Column names
	 * @param rows		Rows
	 */
	public DataTable(List<String> cols, String[][] rows) {
		if(cols.size() != rows[0].length) throw new IllegalArgumentException("Row-column size mismatch");
		this.cols = new ArrayList<>(cols);
		this.rows = rows.clone();
	}

	/**
	 * Retrieves an element from this data-table.
	 * @param row Row index
	 * @param col Column name
	 * @return Element
	 */
	public String get(int row, String col) {
		final int index = getIndex(col);
		return rows[row][index];
	}

	/**
	 * Retrieves a row from the table.
	 * @param name Row name
	 * @return Row
	 */
	public Stream<String> getRow(int index) {
		return Arrays.stream(rows[index]);
	}

	/**
	 * @return Row identifiers (i.e. first element)
	 */
	public Stream<String[]> getRows() {
		return Arrays.stream(rows);
	}

	/**
	 * Retrieves the specified column as a map indexed by the row identifier.
	 * @param col			Column name
	 * @param converter		Converter
	 * @return Column
	 */
	public <T> Map<String, T> getColumn(String col, Converter<T> converter, boolean lowercase) {
		final int index = getIndex(col);
		final Map<String, T> map = new HashMap<>();
		Arrays.stream(rows).forEach(row -> map.put(lowercase ? row[0].toLowerCase() : row[0], converter.convert(row[index])));
		return map;
	}

	public <T> Map<String, T> getColumn(String col, Converter<T> converter) {
		return getColumn(col, converter, false);
	}

	/**
	 * Retrieves the specified column of the data-table as a map ordered by the given enumeration.
	 * <p>
	 * Assumes the first column is the enumeration key (case-sensitive).
	 * <p>
	 * @param col			Column name
	 * @param clazz			Data type
	 * @param converter		Element converter
	 * @param <T> Data-type
	 * @return Column ordered by enumeration
	 * @throws IllegalArgumentException if the number of rows does not match the enumeration
	 * @throws NumberFormatException if a table element cannot be converted or an enumeration key cannot be found
	 */
	public <E extends Enum<E>, T> EnumMap<E, T> getColumn(String col, Class<E> type, Converter<T> converter) throws NumberFormatException {
		// Verify table
		final Collection<E> keys = Arrays.asList(type.getEnumConstants());
		if(rows.length != keys.size()) throw new IllegalArgumentException("Column-row size mismatch");

		// Build map
		final int index = getIndex(col);
		final EnumMap<E, T> map = new EnumMap<>(type);
		for(final String[] row : rows) {
			final E key = Util.getEnumConstant(row[0], type);
			final T value = converter.convert(row[index]);
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Helper - Maps the column name to an index
	 * @param col Column name
	 * @return Column index
	 */
	private int getIndex(String col) {
		final int index = cols.indexOf(col);
		if(index == -1) throw new IllegalArgumentException("Unknown column: " + col);
		return index;
	}

	/**
	 * Loads a data-table from the given source.
	 * @return Data-table
	 * @throws IOException
	 */
	public static DataTable load(Reader r) throws IOException {
		try(final BufferedReader in = new BufferedReader(r)) {
			// Load column names
			final List<String> cols = Arrays.asList(in.readLine().trim().split("\\s+"));
			if(cols.isEmpty()) throw new IllegalArgumentException("Expected columns header");

			// Check number of elements matches columns
			final Consumer<String[]> check = tokens -> {
				if(tokens.length != cols.size()) throw new IllegalArgumentException("Invalid number of elements: " + Arrays.asList(tokens));
			};

			// Load table
			final String[][] rows = in.lines()
				.map(String::trim)
				.filter(StreamUtil.not(String::isEmpty))
				.map(line -> line.split("\\s+"))
				.peek(check)
				.toArray(String[][]::new);

			// Create table
			return new DataTable(cols, rows);
		}
	}

	@Override
	public String toString() {
		return cols.toString();
	}
}
