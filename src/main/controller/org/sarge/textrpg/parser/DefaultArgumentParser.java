package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.entity.CommandArgumentFactory;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.WordCursor;

/**
 * A <i>default<i> argument parser matches a command word against a set of candidate command arguments.
 * @author Sarge
 * @param <T> Data-type
 * @see CommandArgument
 */
public class DefaultArgumentParser<T extends CommandArgument> implements ArgumentParser<T> {
	private final CommandArgumentFactory<T> factory;
	private final Entity actor;

	/**
	 * Constructor.
	 * @param factory 		Candidates argument factory
	 * @param actor			Actor
	 */
	public DefaultArgumentParser(CommandArgumentFactory<T> factory, Entity actor) {
		this.factory = notNull(factory);
		this.actor = notNull(actor);
	}

	@Override
	public T parse(WordCursor cursor) {
		return ArgumentParser.matches(cursor, factory.stream(actor));
	}
}
