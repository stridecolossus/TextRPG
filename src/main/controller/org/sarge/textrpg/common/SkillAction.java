package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;

/**
 * Adapter for a skill-based action.
 * @author Sarge
 */
public abstract class SkillAction extends AbstractAction {
	private final Skill skill;

	/**
	 * Constructor for an action with a required skill.
	 * @param skill Required skill
	 * @param flags Over-ridden action flags
	 */
	protected SkillAction(Skill skill, Flag... flags) {
		super(flags);
		this.skill = notNull(skill);
	}

	@Override
	public int power(Entity actor) {
		return skill(actor).power();
	}

	/**
	 * Helper - Looks up the skill for the given actor.
	 * @param actor Actor
	 * @return Skill
	 * @throws IllegalStateException if the skill is mandatory but not present
	 */
	protected Skill skill(Entity actor) {
		final SkillSet set = actor.skills();
		if(set.contains(skill)) {
			return set.find(skill);
		}
		else {
			if(skill.isMandatory()) throw new IllegalStateException("Mandatory skill not present: " + skill);
			return skill;
		}
	}

	@Override
	public void verify(Entity actor) throws ActionException {
		// Delegate
		super.verify(actor);

		// Check for required skill
		if(skill.isMandatory() && !actor.skills().contains(skill)) {
			throw ActionException.of("action.required", skill.name());
		}
	}
}
