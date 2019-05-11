package org.sarge.textrpg.runner;

import java.io.IOException;
import java.util.Map;

import org.sarge.lib.collection.StrictMap;
import org.sarge.textrpg.util.BandingTable;
import org.sarge.textrpg.util.DataSource;
import org.sarge.textrpg.util.Percentile;
import org.springframework.context.annotation.Configuration;

/**
 * Banding table repository.
 * @author Sarge
 */
@Configuration("banding")
public class BandingTableConfiguration {
	private final Map<String, BandingTable<Percentile>> tables = new StrictMap<>();

	/**
	 * Constructor.
	 * @param src Root data source
	 * @throws IOException if the tables cannot be loaded
	 */
	public BandingTableConfiguration(DataSource src) throws IOException {
		final DataSource folder = src.folder("banding");
		final BandingTable.Loader<Percentile> loader = new BandingTable.Loader<>(Percentile.CONVERTER);
		for(String name : folder.enumerate()) {
			final BandingTable<Percentile> t = loader.load(folder.open(name));
			tables.put(strip(name), t);
		}
	}

	/**
	 * @return Banding tables ordered by name
	 */
	public Map<String, BandingTable<Percentile>> tables() {
		return Map.copyOf(tables);
	}

	/**
	 * Looks up a banding table by name.
	 * @param name Table name
	 * @return Banding table
	 */
	public BandingTable<Percentile> table(String name) {
		if(!tables.containsKey(name)) throw new IllegalArgumentException("Unknown banding table: " + name);
		return tables.get(name);
	}

	/**
	 * Strips the file extension from the given filename.
	 * @param filename Filename
	 * @return Stripped filename
	 */
	private static String strip(String filename) {
		final int index = filename.lastIndexOf('.');
		if(index == -1) {
			return filename;
		}
		else {
			return filename.substring(0, index);
		}
	}
}
