package org.sarge.textrpg.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Util;

/**
 * Table for banding descriptions.
 * @author Sarge
 */
public class BandingTable {
	/**
	 * Table entry.
	 */
	private static class Entry {
		private final float value;
		private final String name;
		
		private Entry(float value, String name) {
			this.value = value;
			this.name = name;
		}
	}
	
	private final List<Entry> table;

	/**
	 * Constructor.
	 * @param table Banding table
	 */
	private BandingTable(List<Entry> table) {
		this.table = new ArrayList<>(table);
	}

	/**
	 * Finds the banding table entry for the given value.
	 * @param value Value
	 * @return Band
	 */
	public String get(float value) {
		final Iterator<Entry> itr = table.iterator();
		while(true) {
			final Entry e = itr.next();
			if(!itr.hasNext()) return e.name;
			if(value <= e.value) return e.name;
		}
	}
	
	@Override
	public String toString() {
		return Arrays.asList(table).toString();
	}
	
	/**
	 * Builder for a banding table.
	 */
	public static class Builder {
		private final List<Entry> table = new ArrayList<>();

		/**
		 * Adds a banding entry.
		 * @param value		Value
		 * @param name		Name
		 * @throws IllegalArgumentException if the entry does not exceed the previous entry
		 */
		public Builder add(float value, String name) {
			Check.notNull(name);
			Check.notEmpty(name);

			// Check exceeds previous entry
			if(!table.isEmpty()) {
				final Entry prev = table.get(table.size() - 1);
				if(value <= prev.value) throw new IllegalArgumentException("Table entries must increase in value: " + name);
			}
			
			// Check for duplicate entries
			if(table.stream().map(e -> e.name).anyMatch(e -> e.equals(name))) throw new IllegalArgumentException("Duplicate entry: " + name);
			
			// Add entry
			table.add(new Entry(value, name));
			return this;
		}

		/**
		 * Builds this table.
		 * @return New banding table
		 * @throws IllegalArgumentException if the table is empty
		 */
		public BandingTable build() {
			if(table.isEmpty()) throw new IllegalArgumentException("Empty table");
			return new BandingTable(table);
		}
	}
	
	/**
	 * Loads a banding table.
	 * @param in Input
	 * @return Banding table
	 * @throws IOException if the table cannot be loaded
	 */
	public static BandingTable load(Reader in) throws IOException {
		final Builder builder = new Builder();
		try(BufferedReader r = new BufferedReader(in)) {
			r.lines().map(String::trim).filter(Util.not(String::isEmpty)).forEach(line -> parse(line, builder));
		}
		return builder.build();
	}

	/**
	 * Parses a table entry.
	 * @param line Table entry line
	 * @return Table entry
	 * @throws IllegalArgumentException if the line is invalid
	 * @throws NumberFormatException if the value cannot be parsed
	 */
	private static void parse(String line, Builder builder) {
		final String tokens[] = line.split(" ");
		if(tokens.length != 2) throw new IllegalArgumentException("Invalid table entry: " + line);
		final float value = Float.parseFloat(tokens[0].trim());
		builder.add(value, tokens[1]);
	}
}
