package org.sarge.textrpg.entity;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;

/**
 * Action to back-stab an enemy.
 * @author Sarge
 */
public class BackStabAction extends AbstractAction {
	private final Skill skill;
	private final long duration;
	
	/**
	 * Constructor.
	 * @param skill			Back-stab skill
	 * @param duration		Base duration (ms)
	 */
	public BackStabAction(Skill skill, long duration) {
		Check.notNull(skill);
		Check.oneOrMore(duration);
		this.skill = skill;
		this.duration = duration;
	}
	
	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
	}
	
	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	/**
	 * Back-stab.
	 * @param actor
	 * @param target
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse backstab(Entity actor, Entity target) throws ActionException {
		// Check required skill
		final int level = getSkillLevel(actor, skill);
		
		// Check valid target
		if(!ActionHelper.isValidTarget(actor, target)) throw new ActionException("backstab.invalid.target");
		
		// Check stance
		if(actor.getStance() != Stance.SNEAKING) throw new ActionException("backstab.not.sneaking");
		
		// Check equipped weapon
		// TODO - backstab.no.weapon
		
		// Start back-stab
		final long duration = calculateDuration(this.duration, level);
		final Induction induction = () -> {
			// TODO
			return new Description("");
		};
		return new ActionResponse("backstab.start", induction, duration);
	}
}
