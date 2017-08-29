package org.sarge.textrpg.entity;

import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

/**
 * Action to bandage a wound.
 * @author Sarge
 */
public class BandageAction extends AbstractAction {
	private final Skill skill;
	private final long duration;
	private final Predicate<WorldObject> bandage;

	/**
	 * Constructor.
	 * @param skill			Required skill
	 * @param duration		Base duration(ms)
	 * @param bandage		Bandage descriptor
	 */
	public BandageAction(Skill skill, long duration, ObjectDescriptor bandage) {
		Check.notNull(skill);
		Check.oneOrMore(duration);
		this.skill = skill;
		this.duration = duration;
		this.bandage = ContentsHelper.objectMatcher(bandage);
	}

	@Override
	public boolean isValidStance(Stance stance) {
	    if(stance == Stance.MOUNTED) {
	        return false;
	    }
	    else {
	        return super.isValidStance(stance);
	    }
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}

	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Bandage self.
	 * @param ctx
	 * @param actor
	 * @throws ActionException
	 */
	@Override
	public ActionResponse execute(Entity actor) throws ActionException {
		return execute(actor, actor);
	}

	/**
	 * Bandage another entity.
	 * @param ctx
	 * @param actor
	 * @param entity
	 * @throws ActionException
	 */
	public ActionResponse execute(Entity actor, Entity entity) throws ActionException {
		// Check can bandage the entity
		if(ActionHelper.isValidTarget(actor, entity)) throw new ActionException("bandage.invalid.target");

		// Check required skill
		final int level = getSkillLevel(actor, skill);

		// Find un-bandaged wound
		// entity.getAppliedEffects().filter(predicate)
		// TODO

		// Check for available bandages
		final WorldObject bandage = find(actor, this.bandage, true, "bandage");

		// Start bandaging
		final Induction induction = () -> {
			// Check able to bandage this wound

			// Apply bandage
			// TODO
			// - wound effect -> bandaged
			// - partial success?

			// Consume bandage
			// TODO - needs to be public!
			// bandage.destroy();
			return new Description("bandage.success");
		};
		return new ActionResponse("bandage.start", induction, duration);
	}
}
