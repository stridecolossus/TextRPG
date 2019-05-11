package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.util.Description;

/**
 * Condition that requires the actor to belong to a given racial category.
 * @author Sarge
 * @see Actor#isCategory(String)
 */
public class RaceCondition extends AbstractEqualsObject implements Condition {
	private final String cat;
	private final Description description;

	/**
	 * Constructor.
	 * @param cat Race category
	 */
	public RaceCondition(String cat) {
		this.cat = notEmpty(cat);
		this.description = new Description("condition.race", cat);
	}

	@Override
	public boolean matches(Actor actor) {
		return actor.isRaceCategory(cat);
	}

	@Override
	public Description reason() {
		return description;
	}
}
