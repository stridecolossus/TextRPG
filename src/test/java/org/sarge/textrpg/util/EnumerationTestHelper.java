package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.EnumSet;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;

/**
 * Test helper for enumerations.
 * @author Sarge
 * @param <E> Enumeration class
 * @param <T> Data-type
 */
public class EnumerationTestHelper<E extends Enum<E>, T> {
	private final EnumSet<E> set;
	private final Function<E, T> mapper;

	/**
	 * Constructor.
	 * @param clazz			Enumeration class
	 * @param mapper		Transformer
	 */
	public EnumerationTestHelper(Class<E> clazz, Function<E, T> mapper) {
		this.set = EnumSet.allOf(clazz);
		this.mapper = notNull(mapper);
	}

	/**
	 * Asserts that the given enumeration constants match the expected value.
	 * @param expected		Expected result
	 * @param values		Enumeration constants
	 * @throws IllegalArgumentException if any constant has already been tested
	 */
	@SuppressWarnings("unchecked")
	public void assertEquals(T expected, E... values) {
		for(E e : values) {
			test(expected, e);
		}
	}

	/**
	 * Asserts that the remaining enumeration constants match the expected value.
	 * @param expected Expected value
	 * @throws IllegalArgumentException if there are no remaining constants
	 */
	public void assertEquals(T expected) {
		if(set.isEmpty()) throw new IllegalArgumentException("No remaining constants");

		for(E e : set) {
			test(expected, e);
		}
	}

	/**
	 * Asserts that the given enumeration constant matches the expected value.
	 * @param expected		Expected result
	 * @param value			Enumeration constant
	 * @throws IllegalArgumentException if the given constant has already been tested
	 */
	private void test(T expected, E value) {
		if(!set.contains(value)) throw new IllegalArgumentException("Enumeration constant already used: " + value);
		Assertions.assertEquals(expected, mapper.apply(value));
		set.remove(value);
	}
}
