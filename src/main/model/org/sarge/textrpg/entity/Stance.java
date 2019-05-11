package org.sarge.textrpg.entity;

import org.sarge.lib.util.Converter;

/**
 * Entity stances.
 * @author Sarge
 */
public enum Stance {
	/**
	 * Default stance (standing, idle).
	 */
	DEFAULT,

	/**
	 * Resting or sitting.
	 */
	RESTING,

	/**
	 * Sleeping (lying down).
	 */
	SLEEPING,

	/**
	 * Mounted.
	 */
	MOUNTED,

	/**
	 * Swimming.
	 */
	SWIMMING,

	/**
	 * Sneaking.
	 */
	SNEAKING,

	/**
	 * Hiding.
	 */
	HIDING;

	/**
	 * Stance converter.
	 */
	public static final Converter<Stance> CONVERTER = Converter.enumeration(Stance.class);

	/**
	 * @return Whether this is a visibility modifying stance
	 */
	public boolean isVisibilityModifier() {
		return (this == SNEAKING) || (this == HIDING);
	}
}
