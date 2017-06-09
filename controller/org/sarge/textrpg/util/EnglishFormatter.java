package org.sarge.textrpg.util;

/**
 * English text formatter.
 * @author Sarge
 * 
 * TODO
 * - a -> an if followed by vowel
 * - append full-stop
 * 
 * str = str.replaceAll(" {2,}", " "); -- remove all double-spaces
 * str = str.replaceAll("<b>([^<]*)</b>", "$1"); -- strip HTML tags
 * (?i) makes the regex case insensitive.
 * http://www.vogella.com/tutorials/JavaRegularExpressions/article.html
 * 
 */
public class EnglishFormatter {
	/**
	 * Formats the given text.
	 * @param text Text to format
	 * @return Formatted text
	 */
	public String format(String text) {
		return text.trim().replaceAll("\\ba ([aeiouAEIOU])", "an $1").replaceAll(" {2,}", " ");
	}
}
