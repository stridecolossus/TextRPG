package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Map;

import org.sarge.lib.collection.StrictMap;
import org.springframework.stereotype.Component;

/**
 * Model for the the snow level at locations.
 * @author Sarge
 */
@Component
public class SnowModel implements WeatherController.Listener {
	/**
	 * Record for modified snow level at a location.
	 */
	private static class Entry {
		private int level;				// Unmodified snow level from weather
		private int mod;				// Snow modification
		private int lifetime;			// Lifetime of this entry

		/**
		 * Constructor.
		 * @param level			Initial snow level
		 * @param lifetime		Lifetime of this entry (per update iteration)
		 */
		private Entry(int level, int lifetime) {
			this.level = oneOrMore(level);
			this.lifetime = oneOrMore(lifetime);
		}

		/**
		 * Reduces (and clamps) the snow level by the given amount.
		 * @param amount Amount to reduce
		 */
		private void reduce(int amount) {
			mod += amount;
			level = Math.max(0, level - amount);
			verify();
		}

		/**
		 * Updates the snow level due to weather.
		 * @param level Snow level from weather
		 */
		private void update(int level) {
			this.level = Math.max(0, level - mod);
			verify();
		}

		/**
		 * Ages this entry.
		 */
		private void age() {
			--lifetime;
			verify();
		}

		/**
		 * @return Whether this entry has expired
		 */
		private boolean isExpired() {
			return lifetime < 1;
		}

		/**
		 * Verifies this entry.
		 */
		private void verify() {
			assert level >= 0;
			assert mod > 0;
			assert lifetime >= 0;
		}
	}

	private final Map<Location, Entry> entries = new StrictMap<>();

	/**
	 * Looks up the unmodified snow level due to weather at the given location.
	 * @param loc Location
	 * @return Snow level
	 */
	private static int get(Location loc) {
		return loc.area().weather().snow();
	}

	/**
	 * Looks up the snow level at the given location.
	 * @param loc Location
	 * @return Snow level
	 */
	public int snow(Location loc) {
		final Entry entry = entries.get(loc);
		if(entry == null) {
			return get(loc);
		}
		else {
			return entry.level;
		}
	}

	/**
	 * Reduces snow level at the given location.
	 * @param loc			Location
	 * @param amount		Amount of snow to remove
	 * @param lifetime		Lifetime of this modification
	 * @return New snow level
	 */
	public int reduce(Location loc, int amount, int lifetime) {
		// Lookup initial snow level due to weather
		final int level = get(loc);
		if(level == 0) throw new IllegalArgumentException("Snow level cannot be reduced");

		// Create new entry
		final Entry entry = entries.computeIfAbsent(loc, key -> new Entry(level, lifetime));

		// Reduce level
		entry.reduce(amount);

		return entry.level;
	}

	/**
	 * Updates all entries after a weather iteration.
	 */
	@Override
	public void update() {
		// Cull expired entries
		entries.values().forEach(Entry::age);
		entries.entrySet().removeIf(entry -> entry.getValue().isExpired());

		// Update entries
		for(Map.Entry<Location, Entry> entry : entries.entrySet()) {
			final int level = get(entry.getKey());
			entry.getValue().update(level);
		}
	}
}
