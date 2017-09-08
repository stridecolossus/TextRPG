package org.sarge.textrpg.entity;

import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractActiveAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Corpse;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Induction;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.object.WorldObject;

/**
 * Action to butcher a corpse.
 * @author Sarge
 */
public class ButcherAction extends AbstractActiveAction {
	/**
	 * Object category for a knife that can be used to butcher a corpse.
	 */
	public static final String KNIFE_CATEGORY = "butchery.knife";

	/**
	 * Knife matcher.
	 */
	private static final Predicate<WorldObject> MATCHER = ContentsHelper.categoryMatcher(KNIFE_CATEGORY);

	private final Skill butchery;
	private final long duration;

	/**
	 * Constructor.
	 * @param butchery		Skill for butchery
	 * @param duration		Base duration (ms)
	 */
	public ButcherAction(Skill butchery, long duration) {
		Check.notNull(butchery);
		Check.oneOrMore(duration);
		this.butchery = butchery;
		this.duration = duration;
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}

	/**
	 * Butcher corpse.
	 * @param actor
	 * @param obj
	 * @throws ActionException
	 */
	public ActionResponse butcher(Entity actor, Corpse corpse) throws ActionException {
		// Check not already butchered
		if(corpse.isButchered()) throw new ActionException("corpse.already.butchered");

		// Check has required skill
		final int level = getSkillLevel(actor, butchery);
		final long duration = calculateDuration(this.duration, level);

		// Check for suitable knife
		final WorldObject knife = find(actor, MATCHER, false, "knife");

		// Butcher corpse
		final Induction induction = () -> {
			corpse.butcher(actor);
			knife.use();
			return new Description("butcher.success");
		};
		return new ActionResponse("butcher.start", induction, duration);
	}

	/**
	 * Attempt to butcher something.
	 * @param actor
	 * @param obj
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse butcher(Entity actor, WorldObject obj) throws ActionException {
		throw new ActionException("butcher.not.corpse");
	}
}
