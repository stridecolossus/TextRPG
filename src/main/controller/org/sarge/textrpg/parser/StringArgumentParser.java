package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notEmpty;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.util.WordCursor;

/**
 * A <i>string</i> argument parser checks that the next word matches a given literal string.
 * @author Sarge
 */
public class StringArgumentParser extends AbstractObject implements ArgumentParser<String> {
	private final String literal;

	/**
	 * Constructor.
	 * @param literal Literal string
	 */
	public StringArgumentParser(String literal) {
		this.literal = notEmpty(literal);
	}

	@Override
	public String parse(WordCursor cursor) {
		if(cursor.matches(literal)) {
			return literal;
		}
		else {
			return null;
		}
	}
}
