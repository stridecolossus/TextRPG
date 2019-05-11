package org.sarge.textrpg.util;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Random-number utility.
 * @author Sarge
 */
public final class Randomiser {
	/**
	 * Random number generator.
	 */
	public static final Random RANDOM = new Random();

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
	 * Generates a random boolean based on the given percentile.
	 * @param p Percentile
	 * @return Random boolean
	 */
	public static boolean isLessThan(Percentile p) {
		return RANDOM.nextFloat() < p.floatValue();
	}

	/**
	 * Randomly selects an element from the given list.
	 * @param list List
	 * @return Random element
	 * @throws NoSuchElementException if the list is empty
	 */
	public static <T> T select(List<T> list) {
		if(list.isEmpty()) throw new NoSuchElementException();
		final int index = range(list.size());
		return list.get(index);
	}
}
