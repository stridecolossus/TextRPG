package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.CommandArgument;

/**
 * Discussion topic.
 * @author Sarge
 * TODO - script for quest topics? requires NOD/ACCEPT to accept quest unless auto-bestowed?
 */
public final class Topic extends AbstractEqualsObject implements CommandArgument {
	/**
	 * Default discussion topic.
	 */
	public static final Topic DEFAULT = new Topic("topic.default");

	private final String name;

	/**
	 * Constructor.
	 * @param name
	 */
	public Topic(String name) {
		this.name = notEmpty(name);
	}

	/**
	 * @return Topic identifier
	 */
	@Override
	public String name() {
		return name;
	}
}
