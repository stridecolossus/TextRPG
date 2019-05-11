package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.CommandArgument;

/**
 * A word trigger is a pseudo-object that can be activated by speaking a command word, e.g. the word required to open a magic door.
 * @author Sarge
 * TODO - do we need some sort of 'ignore period' or will script be ok?
 */
public class WordTrigger implements CommandArgument {
	private final String name;
	private final Control.Handler handler;

	/**
	 * Constructor.
	 * @param name		Trigger name
	 * @param script	Handler invoked by this trigger
	 */
	public WordTrigger(String name, Control.Handler handler) {
		this.name = notEmpty(name);
		this.handler = notNull(handler);
	}

	@Override
	public String name() {
		return name;
	}

	/**
	 * @return Handler invoked by this trigger
	 */
	public Control.Handler handler() {
		return handler;
	}
}
