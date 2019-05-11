package org.sarge.textrpg.common;

/**
 * A <i>numeric converter</i> transforms integer values to/from numeric tokens, e.g. <tt>one</tt> to/from <tt>1</tt>.
 * @author Sarge
 */
public interface NumericConverter {
	/**
	 * Converts a numeric token to the equivalent integer value.
	 * @param token Numeric token
	 * @return Integer
	 * @throws IllegalArgumentException if the token is not a valid numeric
	 */
	int convert(String token);

	/**
	 * Converts an integer value to the equivalent numeric token.
	 * @param value Integer value
	 * @return Numeric token
	 * @throws IllegalArgumentException if there is no equivalent token for the given integer
	 */
	String convert(int value);

	/**
	 * Determines the day-of-month suffix.
	 * @param day Day-of-month
	 * @return Suffix
	 */
	String suffix(int day);
}
