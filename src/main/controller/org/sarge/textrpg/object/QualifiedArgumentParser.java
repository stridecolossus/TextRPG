package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.util.WordCursor;

/**
 * A <i>qualified</i> argument parser matches a {@link WorldObject} with a qualifier.
 * TODO
 * @author Sarge
 */
public class QualifiedArgumentParser implements ArgumentParser<WorldObject> {
	private final ArgumentParser<WorldObject> parser;

	/**
	 * Constructor.
	 * @param parser Object parser
	 */
	public QualifiedArgumentParser(ArgumentParser<WorldObject> parser) {
		this.parser = notNull(parser);
	}

	@Override
	public WorldObject parse(WordCursor cursor) {

		// TODO - qualifier needs to be translated


		final String expected = cursor.next();
		final WorldObject obj = parser.parse(cursor);
		if(obj == null) return null;

		final boolean matches = obj.descriptor().characteristics().qualifier().map(expected::equals).orElse(false);
		if(!matches) return null;

		return obj;
	}

	@Override
	public int count() {
		return 2;
	}
}
