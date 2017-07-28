package org.sarge.textrpg.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.StreamUtil;

/**
 * Description store.
 * @author Sarge
 */
public class DescriptionStore {
	private static final Logger LOG = Logger.getLogger(Logger.class.getName());

	private static final Predicate<String> NOT_EMPTY = StreamUtil.not(str -> str.isEmpty());

	/**
	 * Repository of stores indexed by locale.
	 */
	public static class Repository {
		private final Map<Locale, DescriptionStore> repository = new HashMap<>();

		/**
		 * Constructor.
		 */
		public Repository() {
			repository.put(Locale.ROOT, new DescriptionStore(null));
		}

		/**
		 * Loads from the given directory.
		 * @param root			Root directory
		 * @param prepend		Whether to prepend all keys with the filename
		 * @throws IOException if the files cannot be loaded
		 * @see DescriptionStore#add(String, String)
		 */
		public void load(File root) throws IOException {
			if(!root.isDirectory()) throw new IllegalArgumentException("Not a directory: " + root);

			// Load properties
			Files.walk(root.toPath())
				.filter(Files::isRegularFile)
				.filter(p -> p.toString().endsWith(".properties"))
				.forEach(p -> load(p));
		}

		/**
		 * Loads properties from the given path and adds to the repository.
		 * @param path Path
		 */
		private void load(Path path) {
			// Determine locale
			final Locale locale;
			final String filename = path.getFileName().toString();
			final int start = filename.indexOf("_");
			final int end = filename.indexOf(".properties");
			if(start == -1) {
				locale = Locale.ROOT;
			}
			else {
				final String name = filename.substring(start, end);
				locale = Locale.forLanguageTag(name);
			}

			// Lookup or create store for this locale
			final DescriptionStore store = find(locale);

			// Load file
			try {
				Files.newBufferedReader(path).lines()
					.map(String::trim)
					.filter(NOT_EMPTY)
					.filter(line -> !line.startsWith("#"))
					.map(line -> parse(line))
					.forEach(tokens -> store.add(tokens[0], tokens[1]));
			}
			catch(final Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Parses a property entry.
		 */
		private static String[] parse(String line) {
			final String tokens[] = line.trim().split("[:|=]", 2);
			if(tokens.length != 2) throw new IllegalArgumentException("Invalid property entry: " + line);
			if(tokens[0].isEmpty()) throw new IllegalArgumentException("Empty key: " + line);
			return tokens;
		}

		/**
		 * Finds a store by locale.
		 * @param locale Locale
		 * @return Store
		 */
		public DescriptionStore find(Locale locale) {
			final DescriptionStore existing = repository.get(locale);
			if(existing == null) {
				final DescriptionStore store = new DescriptionStore(findParent(locale));
				repository.put(locale, store);
				return store;
			}
			else {
				return existing;
			}
		}

		/**
		 * Finds the nearest store for the given locale.
		 * @param locale Locale
		 * @return Store
		 */
		private DescriptionStore findParent(Locale locale) {
			Locale parent = locale;
			while(true) {
				parent = toParent(parent);
				final DescriptionStore store = repository.get(parent);
				if(store != null) return store;
			}
		}

		/**
		 * Maps a locale to its parent.
		 * @param locale Locale
		 * @return Parent locale
		 */
		private static Locale toParent(Locale locale) {
			if(!locale.getVariant().isEmpty()) {
				return new Locale(locale.getLanguage(), locale.getCountry());
			}
			else
			if(!locale.getCountry().isEmpty()) {
				return new Locale(locale.getLanguage());
			}
			else {
				return Locale.ROOT;
			}
		}
	}

	private final DescriptionStore parent;
	private final Map<String, String[]> map = new HashMap<>();

	/**
	 * Constructor.
	 * @param parent Parent store
	 */
	private DescriptionStore(DescriptionStore parent) {
		this.parent = parent;
	}

	/**
	 * @return Parent store
	 */
	public DescriptionStore getParent() {
		return parent;
	}

	/**
	 * Looks up an array from this store.
	 * @param key Key
	 * @return Array
	 */
	public String[] getStringArray(String key) {
		final String[] value = map.get(key);
		if(value == null) {
			if(parent == null) {
				LOG.log(Level.SEVERE, "Unknown description key: " + key);
				return new String[]{""};
			}
			else {
				return parent.getStringArray(key);
			}
		}
		else {
			return value;
		}
	}

	/**
	 * Looks up a value from this store.
	 * @param key Key
	 * @return Value
	 */
	public String getString(String key) {
		return getStringArray(key)[0];
	}

	/**
	 * Adds a value to this store.
	 * <p>
	 * A value prefixed with the <tt>@</tt> character can be used to reference existing entries.
	 * @param key		Key
	 * @param value		Value
	 * @throws IllegalArgumentException if the value references an unknown entry
	 */
	public void add(String key, String value) {
		final String str = value.trim();
		if(str.startsWith("@")) {
			final String[] copy = map.get(str.substring(1));
			if(copy == null) {
				throw new IllegalArgumentException("Unknown referenced key: " + value);
			}
			map.put(key, copy);
		}
		else {
			map.put(key, toArray(str));
		}
	}

	/**
	 * Splits a pipe-delimited string.
	 */
	private static String[] toArray(String str) {
		final String[] array = str.split("\\|");
		for(int n = 0; n < array.length; ++n) {
			array[n] = array[n].trim();
		}
		return array;
	}
}
