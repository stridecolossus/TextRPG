package org.sarge.textrpg.common;

import org.sarge.lib.util.Converter;

/**
 * Size enumeration.
 * @author Sarge
 */
public enum Size {
	NONE,
	TINY,
	SMALL,
	MEDIUM,
	LARGE,
	HUGE;

	/**
	 * Size converter.
	 */
	public static final Converter<Size> CONVERTER = Converter.enumeration(Size.class);

	/**
	 * @param size
	 * @return Whether this size is smaller than the given size
	 */
	public boolean isLessThan(Size size) {
		return this.ordinal() < size.ordinal();
	}
}
