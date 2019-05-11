package org.sarge.textrpg.util;

import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;

/**
 * Loader utilities.
 * @author Sarge
 */
public class LoaderHelper {
	private LoaderHelper() {
	}

	/**
	 * Loads an enumeration from the specified child elements.
	 * @param xml				Parent element
	 * @param name				Element name
	 * @param converter			Converter
	 * @return Enumeration
	 */
	public static <E extends Enum<E>> Set<E> enumeration(Element xml, String name, Converter<E> converter) {
		return xml.find(name).stream()
			.flatMap(Element::children)
			.map(Element::name)
			.map(converter::apply)
			.collect(toSet());
	}
}
