package org.sarge.textrpg.object;

import java.util.Map;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.BandingTable;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Sarge
 */
public class BandingArgument extends AbstractEqualsObject {
	/**
	 *
	 */
	@Component
	static final class BandingArgumentFormatter implements ArgumentFormatter {
		@Autowired
		private final Map<String, BandingTable<?>> tables = new StrictMap<>();

		@Override
		public String format(Object arg, NameStore store) {
			final BandingArgument band = (BandingArgument) arg;
			final BandingTable<?> table = tables.get(band.table);
			if(table == null) throw new IllegalArgumentException("");

			table.map(band.value);

			return null;
		}
	}

	private final Percentile value;
	private final String table;

	/**
	 * Constructor.
	 * @param value
	 * @param table
	 */
	public BandingArgument(Percentile value, String table) {
		this.value = value;
		this.table = table;
	}
}
