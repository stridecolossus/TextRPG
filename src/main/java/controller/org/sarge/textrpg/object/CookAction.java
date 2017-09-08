package org.sarge.textrpg.object;

import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Induction;
import org.sarge.textrpg.entity.Skill;

/**
 * Cooks some {@link Food}.
 * @author Sarge
 */
public class CookAction extends AbstractAction {
	private final Skill skill;
	private final Predicate<WorldObject> utensil;
	private final Predicate<WorldObject> fire;
	private final long duration;

	/**
	 * Constructor.
	 * @param skill			Cooking skill
	 * @param utensil		Utensil category
	 * @param fire			Cooking-fire category
	 * @param duration		Base cook duration (ms)
	 */
	public CookAction(Skill skill, String utensil, String fire, long duration) {
		Check.notNull(skill);
		Check.notEmpty(utensil);
		Check.notEmpty(fire);
		Check.oneOrMore(duration);
		this.skill = skill;
		this.utensil = ContentsHelper.categoryMatcher(utensil);
		this.fire = ContentsHelper.categoryMatcher(fire);
		this.duration = duration;
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	/**
	 * Cooks some food.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse cook(Entity actor, Food food) throws ActionException {
		// Check required skill
		final int level = getSkillLevel(actor, skill);

		// Check can be cooked
		food.verifyCook();

		// Check cooking utensil available
		find(actor, utensil, false, "utensil");
		
		// Check cooking fire available
		ContentsHelper.select(actor.location().contents().stream(), WorldObject.class)
			.filter(fire)
			.findFirst()
			.orElseThrow(() -> new ActionException("cook.requires.fire"));

		// Start cooking
		final Induction induction = () -> {
			food.cook();
			return new Description("cook.finished", "name", food.name());
		};
		return new ActionResponse("cook.start", induction, super.calculateDuration(duration, level));
	}
}
