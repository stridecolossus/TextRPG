package org.sarge.textrpg.util;

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.sarge.lib.util.Converter;
import org.sarge.lib.util.StreamUtil;

/**
 * 2D table of string values.
 * @author Sarge
 * @param <E> Enumeration
 */
public class DataTable {
    private static final String WHITE_SPACE_PATTERN = "\\s+";
    private static final Predicate<String> NOT_EMPTY = StreamUtil.not(String::isEmpty);   // TODO - move to util

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
     * @throws NumberFormatException if a table element cannot be converted
	 * @throws IllegalStateException if this table contains duplicate row identifier
	 */
	public <T> Map<String, T> getColumn(String col, Converter<T> converter) {
		final int index = getIndex(col);
		return Arrays.stream(rows).collect(toMap(row -> row[0], row -> converter.convert(row[index])));
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
			final List<String> cols = Arrays.asList(in.readLine().trim().split(WHITE_SPACE_PATTERN));
			if(cols.isEmpty()) throw new IllegalArgumentException("Expected columns header");

			// Check number of elements matches columns
			final Consumer<String[]> check = tokens -> {
				if(tokens.length != cols.size()) {
				    throw new IllegalArgumentException("Invalid number of elements: " + Arrays.asList(tokens));
				}
			};

			// Load table
			final String[][] rows = in.lines()
				.map(String::trim)
				.filter(NOT_EMPTY)
				.map(line -> line.split(WHITE_SPACE_PATTERN))
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
