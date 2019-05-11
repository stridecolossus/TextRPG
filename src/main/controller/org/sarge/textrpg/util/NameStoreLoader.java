package org.sarge.textrpg.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Map;
import java.util.function.Predicate;

import org.sarge.lib.collection.StrictMap;

/**
 * Loader for a {@link NameStore}.
 * <p>
 * Name-store files have a YAML-like syntax as follows:
 * <ul>
 * <li>Entries are delimited by the colon character</li>
 * <li>Array values are specified by a pipe-delimited string</li>
 * <li>Comments are prefixed by the hash character</li>
 * <li>Empty lines are ignored</li>
 * <li>Entries indented with a tab character are children with the key prefixed with that of the preceding entry</li>
 * </ul>
 * <p>
 * Examples:
 * <p>
 * <pre>
 *   # Simple entry
 *   key: value
 *
 *   # Array entry
 *   key: one | two
 *
 *   # Indentation
 *   parent: value
 *       child: value
 *       ...
 * </pre>
 * <p>
 * @author Sarge
 */
public class NameStoreLoader {
	private static final Predicate<String> NOT_COMMENT = line -> !line.startsWith("#");
	private static final Predicate<String> NOT_EMPTY = line -> !line.isEmpty();

	private final Map<String, String> names = new StrictMap<>();
	private final Map<String, String[]> arrays = new StrictMap<>();

	private String prev;

	/**
	 * Loads a name-store from the given reader.
	 * @param r Reader
	 * @throws IOException if the name-store cannot be loaded
	 */
	public void load(Reader r) throws IOException {
		try(final LineNumberReader in = new LineNumberReader(r)) {
			try {
				in.lines().filter(NOT_COMMENT).filter(NOT_EMPTY).forEach(this::load);
			}
			catch(RuntimeException e) {
				throw new IOException(e.getMessage() + " at line " + in.getLineNumber(), e);
			}
		}
		prev = null;
	}

	/**
	 * Load the given line.
	 * @param line Line
	 */
	private void load(String line) {
		// Trim line and check for indented entry
		final boolean indented;
		final String trimmed;
		if(line.charAt(0) == '\t') {
			if(prev == null) throw new IllegalStateException("No previous entry for indentation");
			trimmed = prev + "." + line.trim();
			indented = true;
		}
		else {
			trimmed = line.trim();
			indented = false;
		}

		// Split key-names
		final int index = trimmed.indexOf(':');
		if(index == -1) throw new IllegalArgumentException("Expected colon delimiter");

		// Extract key
		final String key = trimmed.substring(0, index).trim();
		if(key.isEmpty()) throw new IllegalArgumentException("Key cannot be empty");

		// Extract value
		final String value = trimmed.substring(index + 1).trim();
		if(value.isEmpty()) throw new IllegalArgumentException("Value cannot be empty");

		// Add entry
		final String[] array = value.split("\\|");
		if(array.length == 1) {
			names.put(key, value);
		}
		else {
			for(int n = 0; n < array.length; ++n) {
				array[n] = array[n].trim();
				if(array[n].isEmpty()) throw new IllegalArgumentException("Names in array cannot be empty");
			}
			arrays.put(key, array);
		}

		// Note previous entry
		if(!indented) {
			prev = key;
		}
	}

	/**
	 * Constructs this name-store.
	 * @return New name-store
	 */
	public NameStore build() {
		return NameStore.of(new DefaultNameStore(names), new ArrayNameStore(arrays));
	}
}
