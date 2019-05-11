package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.xml.Element;
import org.springframework.stereotype.Service;

/**
 * Loader for a liquid descriptor.
 * @author Sarge
 */
@Service
public class LiquidLoader {
	private final EffectLoader loader;

	/**
	 * Constructor.
	 * @param loader Effect loader
	 */
	public LiquidLoader(EffectLoader loader) {
		this.loader = notNull(loader);
	}

	/**
	 * Loads a liquid.
	 * @param xml XML
	 * @return Liquid
	 */
	public Liquid load(Element xml) {
		final String name = xml.attribute("name").toText();
		final Effect effect = loader.load(xml.child());
		final Effect.Group curative = xml.attribute("curative").toValue(Effect.Group.DEFAULT, EffectLoader.GROUP);
		return new Liquid(name, effect, curative);
	}
}
