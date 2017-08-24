package org.sarge.textrpg.util;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.LoaderException;

/**
 * Registry.
 * @param <T> Type
 */
public class Registry<T> {
	private final Map<String, T> map = new StrictMap<>();
	private final Function<T, String> mapper;

	/**
	 * Constructor.
	 * @param mapper Maps this type to its name
	 */
	public Registry(Function<T, String> mapper) {
		this.mapper = mapper;
	}

	/**
	 * Constructor.
	 * @param mapper Maps this type to its name
	 */
	public Registry(Set<T> set, Function<T, String> mapper) {
		this(mapper);
		set.forEach(e -> map.put(mapper.apply(e), e));
	}
	
	public void add(T obj) {
		map.put(mapper.apply(obj), obj);
	}

	/**
	 * Registers a new entry.
	 * @param obj Entry
	 */
	public void add(T obj, Element xml) {
		add(mapper.apply(obj), obj, xml);
	}
	
	public void add(String key, T obj, Element xml) {
		if(map.containsKey(key)) throw new LoaderException(xml, "Duplicate: " + key);
		map.put(key, obj);
	}
	
	/**
	 * Looks up an entry by name.
	 * @param name Entry name
	 * @return Entry
	 * @throws IllegalArgumentException if the entry is not present in this registry
	 */
	public T find(String name) {
		final T obj = map.get(name);
		//if(obj == null) throw new IllegalArgumentException("Unknown registry entry: " + name);
		return obj;
	}
}
