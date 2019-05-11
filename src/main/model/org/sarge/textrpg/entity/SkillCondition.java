package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * Condition that requires the actor to possess a given skill.
 * @author Sarge
 * @see Actor#contains(Skill)
 */
public class SkillCondition extends AbstractEqualsObject implements Condition {
	private final Skill skill;
	private final Description description;

	/**
	 * Constructor.
	 * @param skill Required skill
	 */
	public SkillCondition(Skill skill) {
		this.skill = notNull(skill);
		this.description = new Description("condition.skill", TextHelper.join("skill", skill.name()));
	}

	@Override
	public boolean matches(Actor actor) {
		return actor.skills().contains(skill);
	}

	@Override
	public Description reason() {
		return description;
	}
}
