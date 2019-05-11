package org.sarge.textrpg.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.MutableIntegerMap.DefaultEntry;
import org.sarge.textrpg.util.MutableIntegerMap.MutableEntry;

/**
 * Integer map indexed by an enumeration.
 * @param <E> Enumeration type
 */
public class EnumerationIntegerMap<E extends Enum<E>> extends AbstractEqualsObject implements IntegerMap<E> {
	private final Map<E, MutableEntry> map;

	/**
	 * Constructor.
	 * @param clazz Enumeration class
	 */
	public EnumerationIntegerMap(Class<E> clazz) {
		this.map = new EnumMap<>(clazz);
		init(clazz, key -> 0);
	}

	/**
	 * Copy constructor.
	 * @param clazz 	Enumeration class
	 * @param map		Map to copy
	 */
	public EnumerationIntegerMap(Class<E> clazz, IntegerMap<E> map) {
		this.map = new EnumMap<>(clazz);
		init(clazz, key -> map.get(key).get());
	}

	/**
	 * Initialises this enumeration map.
	 * @param clazz		Enumeration class
	 * @param mapper	Looks up an entry
	 */
	protected void init(Class<E> clazz, Function<E, Integer> mapper) {
		for(E key : clazz.getEnumConstants()) {
			final MutableEntry entry = new DefaultEntry();
			final int value = mapper.apply(key);
			entry.set(value);
			map.put(key, entry);
		}
	}

	@Override
	public MutableEntry get(E key) {
		return map.get(key);
	}

	@Override
	public Stream<E> keys() {
		return map.keySet().stream();
	}
}
