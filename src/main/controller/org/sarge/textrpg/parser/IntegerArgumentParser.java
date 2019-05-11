package org.sarge.textrpg.parser;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.util.WordCursor;

/**
 * An <i>integer</i> argument parser is used to parse an <b>integral</b> command word.
 * @author Sarge
 * @see NumericArgumentParser
 */
public class IntegerArgumentParser implements ArgumentParser<Integer> {
	@Override
	public Integer parse(WordCursor cursor) {
		final String word = cursor.next();
		try {
			return Integer.parseInt(word);
		}
		catch(NumberFormatException e) {
			return null;
		}
	}
}
