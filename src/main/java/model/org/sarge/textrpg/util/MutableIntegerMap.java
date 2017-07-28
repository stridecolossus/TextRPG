package org.sarge.textrpg.util;

import java.util.EnumMap;

/**
 * Map of integer values.
 * @author Sarge
 * @param <K> Key
 */
public class MutableIntegerMap<E extends Enum<E>> implements IntegerMap<E> {
	/**
	 * Map entry.
	 */
	private class MutableInteger {
		int value;
		
		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}
	
	private final EnumMap<E, MutableInteger> map;
	
	/**
	 * Constructor for an integer-map with all zero values.
	 * @param clazz Key class
	 */
	public MutableIntegerMap(Class<E> clazz) {
		this.map = new EnumMap<>(clazz);
		set(clazz, 0);
	}
	
	/**
	 * Copy constructor.
	 * @param other Integer-map to copy
	 */
	public MutableIntegerMap(Class<E> clazz, IntegerMap<E> other) {
		this(clazz);
		for(E key : clazz.getEnumConstants()) {
			final MutableInteger entry = new MutableInteger();
			entry.value = other.get(key);
			map.put(key, entry);
		}
	}

	/**
	 * Retrieves a map entry.
	 */
	private MutableInteger lookup(E key) {
		return map.get(key);
	}
	
	@Override
	public int get(E key) {
		return lookup(key).value;
	}

	/**
	 * Sets a map entry.
	 * @param key		Key
	 * @param value		Value
	 */
	public void set(E key, int value) {
		lookup(key).value = value;
	}
	
	/**
	 * Resets all values.
	 * @param class Enumeration
	 * @param value Value to set
	 */
	public void set(Class<E> clazz, int value) {
		map.clear();
		for(E key : clazz.getEnumConstants()) {
			final MutableInteger entry = new MutableInteger();
			entry.value = value;
			map.put(key, entry);
		}
	}
	
	/**
	 * Increments a map entry.
	 * @param key		Key to increment
	 * @param value		Increment value
	 */
	public void add(E key, int value) {
		lookup(key).value += value;
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
}
