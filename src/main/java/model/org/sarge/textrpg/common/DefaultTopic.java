package org.sarge.textrpg.common;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.Trainer;
import org.sarge.textrpg.object.Shop;

/**
 * Conversation topic.
 */
public class DefaultTopic implements Topic {
	private final String name;
	private final Script script;

	/**
	 * Constructor.
	 * @param name		Topic identifier
	 * @param script	Script
	 */
	public DefaultTopic(String name, Script script) {
		Check.notEmpty(name);
		Check.notNull(script);
		this.name = name;
		this.script = script;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Script getScript() {
		return script;
	}
	
	@Override
	public Shop getShop() {
		return null;
	}
	
	@Override
	public Trainer getTrainer() {
		return null;
	}

	@Override
	public String toString() {
		return name;
	}
}
