package org.sarge.textrpg.util;

/**
 * Formats numerals to text and vice-versa.
 * @author Sarge
 */
public interface NumericFormatter {
	/**
	 * Formats the given number to text.
	 * @param num Number to format
	 * @return Numeric text
	 */
	String format(int num);
	
	/**
	 * Formats the given text to a number (ignoring any non-numeric words).
	 * @param text Space-delimited numeric text
	 * @return Number
	 */
	int format(String text);
}
