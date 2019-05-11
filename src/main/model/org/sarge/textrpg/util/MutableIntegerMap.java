package org.sarge.textrpg.util;

import java.util.Map;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Mutable map of integers.
 * @param <T> Key-type
 */
public class MutableIntegerMap<T> extends AbstractEqualsObject implements IntegerMap<T> {
	/**
	 * Mutable entry.
	 */
	public interface MutableEntry extends ValueModifier, Entry {
		/**
		 * Sets this value.
		 * @param value New value
		 */
		void set(int value);
	}

	/**
	 * Default implementation.
	 */
	public static class DefaultEntry extends AbstractEqualsObject implements MutableEntry {
		private int value;

		@Override
		public int get() {
			return value;
		}

		@Override
		public void set(int value) {
			this.value = value;
		}

		/**
		 * Modifies this entry.
		 * @param inc Value increment
		 * @return Modified value
		 */
		@Override
		public int modify(float inc) {
			value += inc;
			return value;
		}
	}

	protected final Map<T, MutableEntry> map = new StrictMap<>();

	@Override
	public MutableEntry get(T key) {
		return map.computeIfAbsent(key, this::create);
	}

	@Override
	public Stream<T> keys() {
		return map.keySet().stream();
	}

	/**
	 * Creates a new entry.
	 * Over-ride in sub-classes for customised entries.
	 * @param key Key
	 * @return New entry
	 */
	protected MutableEntry create(T key) {
		return new DefaultEntry();
	}
}
