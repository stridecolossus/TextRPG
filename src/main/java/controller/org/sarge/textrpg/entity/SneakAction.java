package org.sarge.textrpg.entity;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractActiveAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.util.Percentile;

/**
 * Sneak action.
 * @author Sarge
 */
public class SneakAction extends AbstractActiveAction {
	private final Skill skill;

	/**
	 * Constructor.
	 * @param skill Sneak skill
	 */
	public SneakAction(Skill skill) {
		Check.notNull(skill);
		this.skill = skill;
	}

	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	/**
	 * Toggle sneaking.
	 * @param actor
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse sneak(Entity actor) throws ActionException {
		if(actor.stance() == Stance.SNEAKING) {
			// Stop sneaking
			actor.setStance(Stance.DEFAULT);
			return new ActionResponse(new Description("sneak.stop"));
		}
		else {
			// Check required skill
			final int level = getSkillLevel(actor, skill);
			final Percentile vis = skill.toPercentile(level).invert();

			// Start sneaking
			actor.setStance(Stance.SNEAKING);
			actor.setVisibility(vis);
			return new ActionResponse(new Description("sneak.start"));
		}
	}
}
