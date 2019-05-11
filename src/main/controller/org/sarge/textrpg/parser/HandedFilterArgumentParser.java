package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Filter;
import org.sarge.textrpg.util.WordCursor;
import org.springframework.stereotype.Component;

/**
 * Argument parser for an {@link ObjectDescriptor.Filter} filtering on 1/2 handed objects.
 * @author Sarge
 */
@Component
public class HandedFilterArgumentParser implements ArgumentParser<ObjectDescriptor.Filter> {
	private final ArgumentParser<Integer> numeric;

	/**
	 * Constructor.
	 */
	public HandedFilterArgumentParser(ArgumentParser<Integer> numeric) {
		this.numeric = notNull(numeric);
	}

	@Override
	public Filter parse(WordCursor cursor) {
		// Parse one/two component
		final Integer num = numeric.parse(cursor);
		if(num == null) return null;
		if((num < 1) || (num > 2)) return null;

		// Parse literal component
		final var literal = new StringArgumentParser("handed");
		if(literal.parse(cursor) == null) return null;

		// Determine filter
		return num == 1 ? ObjectDescriptor.Filter.ONE_HANDED : ObjectDescriptor.Filter.TWO_HANDED;
	}

	@Override
	public int count() {
		return 2;
	}
}
