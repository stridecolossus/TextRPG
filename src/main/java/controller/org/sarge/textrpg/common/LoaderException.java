package org.sarge.textrpg.common;

import java.util.function.Function;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.ElementException;

/**
 * Custom XML exception to render <b>name</b> attributes when present.
 * @author Sarge
 */
public class LoaderException extends ElementException {
	/**
	 * Maps an element to its <b>name</b> attribute where available.
	 */
	private static final Function<Element, String> MAPPER = e -> {
		return e.attributes()
			.getOptional("name", Converter.STRING)
			.orElse(ElementException.ELEMENT_NAME.apply(e));
	};
	
	public LoaderException(Element element, String reason) {
		super(element, reason, MAPPER);
	}

	public LoaderException(Element element, Exception e) {
		super(element, e, MAPPER);
	}
}
