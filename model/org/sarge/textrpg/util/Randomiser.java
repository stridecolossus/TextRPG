package org.sarge.textrpg.util;

import java.util.List;
import java.util.Random;

import org.sarge.lib.util.Check;

/**
 * Random number utility.
 * @author Sarge
 */
public final class Randomiser {
	/**
	 * RNG.
	 */
	public static final Random RANDOM = new Random();

	/**
	 * Utility class.
	 */
	private Randomiser() {
	}

	/**
	 * Generates a random integer in the given range.
	 * @param range Range
	 * @return Random integer
	 */
	public static int range(int range) {
		return RANDOM.nextInt(range);
	}
	
	/**
	 * Randomly selects an entry from the given list.
	 * @param list List
	 * @return Random list entry
	 */
	public static <T> T random(List<T> list) {
		Check.notEmpty(list);
		final int index = range(list.size());
		return list.get(index);
	}

	/**
	 * Percentile chance.
	 * @param p Percentile
	 * @return If a randomly generated chance is <b>less-than</b> the given percentile
	 */
	public static boolean percentile(Percentile p) {
		return range(100) < p.intValue();
	}
}
