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
	 * @return Inverted size
	 * @throws RuntimeException for {@link #NONE}
	 */
	public Size invert() {
		switch(this) {
		case TINY:		return HUGE;
		case SMALL:		return LARGE;
		case MEDIUM:	return MEDIUM;
		case LARGE:		return SMALL;
		case HUGE:		return TINY;
		default:		throw new RuntimeException();
		}
	}

	/**
	 * @param size
	 * @return Whether this size is larger than the given size
	 */
	public boolean isLargerThan(Size size) {
		return this.ordinal() > size.ordinal();
	}
}
