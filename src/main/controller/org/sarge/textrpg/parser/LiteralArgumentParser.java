package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.util.WordCursor;

/**
 * A <i>literal</i> argument parser checks that the next word matches a given literal argument.
 * @param <T> Literal type
 * @author Sarge
 */
public class LiteralArgumentParser<T extends CommandArgument> extends AbstractObject implements ArgumentParser<T> {
	private final T literal;

	/**
	 * Constructor.
	 * @param literal Literal
	 */
	public LiteralArgumentParser(T literal) {
		this.literal = notNull(literal);
	}

	@Override
	public T parse(WordCursor cursor) {
		if(cursor.matches(literal.name())) {
			return literal;
		}
		else {
			return null;
		}
	}
}
