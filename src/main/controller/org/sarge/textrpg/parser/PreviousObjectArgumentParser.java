package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.util.WordCursor;

/**
 * Argument parser for the previous object.
 * @author Sarge
 */
public class PreviousObjectArgumentParser extends AbstractObject implements ArgumentParser<Thing> {
	private final PlayerCharacter player;

	/**
	 * Constructor.
	 * @param player Player
	 */
	public PreviousObjectArgumentParser(PlayerCharacter player) {
		this.player = notNull(player);
	}

	@Override
	public Thing parse(WordCursor cursor) {
		if(cursor.matches("previous.object")) {
			return player.previous();
		}
		else {
			return null;
		}
	}
}
