package org.sarge.textrpg.common;

import org.sarge.lib.util.Converter;

/**
 * Entity and faction alignments.
 * @author Sarge
 */
public enum Alignment {
	NEUTRAL,
	GOOD,
	EVIL;

	/**
	 * Converter.
	 */
	public static final Converter<Alignment> CONVERTER = Converter.enumeration(Alignment.class);

	/**
	 * @param alignment Alignment
	 * @return Whether the given alignment is a valid target for this alignment
	 */
	public boolean isValidTarget(Alignment alignment) {
		return alignment != this;
	}
}
