package org.sarge.textrpg.entity;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;

/**
 * Action that toggles whether to swim.
 * @author Sarge
 */
public class SwimAction extends AbstractAction {
	private final Skill skill;
	
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}
	
	@Override
	public boolean isCombatBlockedAction() {
		return super.isCombatBlockedAction();
	}
	
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}
	
	@Override
	public boolean isVisibleAction() {
		return false;
	}
	
	@Override
	public boolean isValidStance(Stance stance) {
		return true;
	}

	/**
	 * Constructor.
	 * @param skill Swimming skill
	 */
	public SwimAction(Skill skill) {
		Check.notNull(skill);
		this.skill = skill;
	}

	/**
	 * Toggle swimming.
	 * @param actor
	 * @return
	 * @throws ActionException if the player does not have the swimming skill
	 */
	public ActionResponse swim(Entity actor) throws ActionException {
		final Player player = (Player) actor;
		if(player.isSwimming()) {
			// Stop swimming
			player.setSwimming(false);
		}
		else {
			// Start swimming
			getSkillLevel(actor, skill);
			player.setSwimming(true);
		}
		return ActionResponse.OK;
	}
}
