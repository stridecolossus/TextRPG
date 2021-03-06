package org.sarge.textrpg.common;

import org.sarge.lib.util.Converter;

/**
 * An <i>emission</i> is something generated by objects and entities that can be detected.
 */
public enum Emission {
	LIGHT,
	SMOKE,
	SOUND;

	/**
	 * Emission converter.
	 */
	public static final Converter<Emission> CONVERTER = Converter.enumeration(Emission.class);
}
