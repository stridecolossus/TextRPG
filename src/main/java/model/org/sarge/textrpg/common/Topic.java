package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.entity.Trainer;
import org.sarge.textrpg.object.Shop;

/**
 * Conversation topic.
 */
public class Topic {
    private final String name;
    private final Script script;

    /**
     * Constructor.
     * @param name Topic name
     */
    public Topic(String name, Script script) {
        this.name = notEmpty(name);
        this.script = notNull(script);
    }

    /**
     * Convenience constructor for a simple message topic.
     * @param name Topic name
     * @param text Message text
     */
    public Topic(String name, String text) {
        this(name, Script.message(text));
    }

    /**
     * @return Topic name
     */
	public final String name() {
	    return name;
	}

	/**
	 * @return Script for this topic
	 */
	public Script script() {
	    return script;
	}

	/**
	 * @return Shop model is this is a shop topic
	 * @throws UnsupportedOperationException by default
	 */
	public Shop shop() {
        throw new UnsupportedOperationException();
    }

	/**
	 * @return Trainer model if this is a trainer topic
     * @throws UnsupportedOperationException by default
	 */
	public Trainer trainer() {
        throw new UnsupportedOperationException();
    }

	@Override
	public String toString() {
	    return name;
	}
}
