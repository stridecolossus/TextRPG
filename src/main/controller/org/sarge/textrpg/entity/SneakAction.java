package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sneak action.
 * @author Sarge
 */
@Component
public class SneakAction extends SkillAction {
	/**
	 * Constructor.
	 * @param skill Sneak skill
	 */
	public SneakAction(@Value("#{skills.get('sneak')}") Skill skill) {
		super(skill);
	}

	/**
	 * Toggles sneaking.
	 * @param actor Actor
	 * @return Response
	 */
	@RequiresActor
	public Response sneak(Entity actor) {
		final EntityModel model = actor.model();
		final Stance current = model.stance();
		final Visibility vis = model.values().visibility();
		if(current == Stance.SNEAKING) {
			// Stop sneaking
			vis.remove();
			model.stance(Stance.DEFAULT);
			// TODO - calculate movement cost modifier and set in movement mode (but how?)
			return Response.of("action.sneak.stop");
		}
		else {
			// Remove hiding modifier
			if(current == Stance.HIDING) {
				vis.remove();
			}

			// Start sneaking
			final Percentile score = super.skill(actor).score().invert();
			vis.stance(score);
			model.stance(Stance.SNEAKING);
			return Response.of("action.sneak.start");
		}
	}
}
