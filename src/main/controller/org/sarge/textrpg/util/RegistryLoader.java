package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.xml.Element;

/**
 * Adapter for a registry that can also load new custom values.
 * @author Sarge
 * @param <T> Registry type
 */
public class RegistryLoader<T> implements Registry<T> {
	private final Registry<T> registry;
	private final Function<Element, T> loader;
	private final String name;

	/**
	 * Constructor.
	 * @param registry		Registry of pre-defined objects
	 * @param loader		Loader for custom objects
	 * @param name			Attribute name
	 */
	public RegistryLoader(Registry<T> registry, Function<Element, T> loader, String name) {
		this.registry = notNull(registry);
		this.loader = notNull(loader);
		this.name = notEmpty(name);
	}

	@Override
	public T get(String name) {
		final T entry = registry.get(name);
		if(entry == null) throw new IllegalArgumentException("Unknown registry entry: " + name);
		return entry;
	}

	/**
	 * Loads or looks-up an object.
	 * @param xml XML
	 * @return New object
	 */
	public T load(Element xml) {
		final var attr = xml.attribute(name);
		if(attr.isPresent()) {
			final T entry = registry.get(attr.toText());
			if(entry == null) throw xml.exception("Unknown registry entry: " + name);
			return entry;
		}
		else {
			return loader.apply(xml);
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
