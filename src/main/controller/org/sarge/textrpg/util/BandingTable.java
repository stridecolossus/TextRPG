package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.StreamUtil;

/**
 * Banding table that maps a comparable value to a descriptive string.
 * @param <T> Banding type
 * @author Sarge
 */
public class BandingTable<T extends Comparable<T>> {
	/**
	 * Banding-table entry.
	 */
	private static class Band<T> {
		private final T value;
		private final String name;

		/**
		 * Constructor.
		 * @param band Band
		 * @param name Name
		 */
		private Band(T band, String name) {
			this.value = notNull(band);
			this.name = notEmpty(name);
		}

		@Override
		public boolean equals(Object that) {
			return EqualsBuilder.reflectionEquals(this, that);
		}

		@Override
		public String toString() {
			return value + " -> " + name;
		}
	}

	/**
	 * Convenience factory for a table with a single entry.
	 * @param value		Banding value
	 * @param name		Name
	 * @return Banding table
	 */
	public static <T extends Comparable<T>> BandingTable<T> of(T value, String name) {
		return new Builder<T>().add(value, name).build();
	}

	private final List<Band<T>> table;

	/**
	 * Constructor.
	 * @param table Banding-table
	 */
	private BandingTable(List<Band<T>> table) {
		this.table = List.copyOf(table);
	}

	/**
	 * @return Maximum banding value in this table
	 */
	public T max() {
		return table.get(table.size() - 1).value;
	}

	/**
	 * Maps the given value to the corresponding name.
	 * @param value Value
	 * @return Band name
	 */
	public String map(T value) {
		for(Band<T> band : table) {
			if(value.compareTo(band.value) < 1) {
				return band.name;
			}
		}

		throw new RuntimeException("Value exceeds maximum band entry: " + value);
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that);
	}

	@Override
	public String toString() {
		return table.toString();
	}

	/**
	 * Builder for a banding-table.
	 */
	public static class Builder<T extends Comparable<T>> {
		private final List<Band<T>> table = new ArrayList<>();
		private T prev;

		/**
		 * Adds a band.
		 * @param value		Band value
		 * @param name 		Name
		 * @throws IllegalArgumentException if the table bands are not ascending
		 */
		public Builder<T> add(T value, String name) {
			final Band<T> band = new Band<>(value, name);
			add(band);
			return this;
		}

		/**
		 * Adds a band.
		 * @param band Band to add
		 * @throws IllegalArgumentException if the table bands are not ascending
		 */
		private void add(Band<T> band) {
			table.add(band);
			if(prev != null) {
				if(band.value.compareTo(prev) < 1) throw new IllegalArgumentException("Bands must be ascending: " + band.name);
			}
			prev = band.value;
		}

		/**
		 * @return New banding-table
		 * @throws IllegalArgumentException if the table is empty
		 */
		public BandingTable<T> build() {
			if(table.isEmpty()) throw new IllegalArgumentException("Empty table");
			return new BandingTable<>(table);
		}
	}

	/**
	 * Loader for a banding-table.
	 */
	public static class Loader<T extends Comparable<T>> {
		private final Converter<T> converter;

		/**
		 * Constructor.
		 * @param converter Band converter
		 */
		public Loader(Converter<T> converter) {
			this.converter = notNull(converter);
		}

		/**
		 * Loads a banding-table.
		 * @param r Reader
		 * @return Banding-table
		 * @throws IOException if the table cannot be loaded
		 */
		public BandingTable<T> load(Reader r) throws IOException {
			final Builder<T> builder = new Builder<>();
			try(final BufferedReader in = new BufferedReader(r)) {
				in.lines()
					.map(String::trim)
					.filter(line -> !line.startsWith("#"))
					.filter(StreamUtil.not(String::isEmpty))
					.map(this::load)
					.forEach(builder::add);
			}
			return builder.build();
		}

		/**
		 * Loads a banding entry.
		 * @param line Line
		 * @return Band
		 */
		private Band<T> load(String line) {
			// Tokenize line
			final String[] tokens = line.trim().split("\\s+");
			if(tokens.length != 2) throw new IllegalArgumentException("Invalid banding: " + line);

			// Convert banding value
			final T band = converter.apply(tokens[0].trim());

			// Create band
			return new Band<>(band, tokens[1].trim());
		}
	}
}
