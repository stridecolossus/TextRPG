package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.NumericConverter;
import org.sarge.textrpg.util.WordCursor;
import org.springframework.stereotype.Component;

/**
 * A <i>numeric</i> argument parser is used to parse an integer <b>or</b> an <i>integer token</i>.
 * @author Sarge
 */
@Component
public class NumericArgumentParser implements ArgumentParser<Integer> {
	private final NumericConverter converter;

	/**
	 * Constructor.
	 * @param converter Integer token mapper
	 */
	public NumericArgumentParser(NumericConverter converter) {
		this.converter = notNull(converter);
	}

	@Override
	public Integer parse(WordCursor cursor) {
		// Try integer first
		final String word = cursor.next();
		try {
			return Integer.parseInt(word);
		}
		catch(NumberFormatException e) {
			// Ignored
		}

		// Otherwise lookup token
		return converter.convert(word);
	}
}
