package org.sarge.textrpg.object;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.common.Trap;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to detect and disarm traps.
 * @author Sarge
 */
@Component
public class TrapDisarmAction extends SkillAction {
	/**
	 * Constructor.
	 * @param skill Disarm-trap skill
	 */
	public TrapDisarmAction(@Value("#{skills.get('disarm')}") Skill skill) {
		super(skill, Flag.LIGHT, Flag.INDUCTION);
	}

	/**
	 * Disarms a detected trap.
	 * @param actor			Actor
	 * @param openable		Openable
	 * @return Response
	 * @throws ActionException if the actor has not detected a trap on the given openable
	 */
	@RequiresActor
	public Response disarm(PlayerCharacter actor, Openable openable) throws ActionException {
		// Check trap can be disarmed
		final Openable.Model model = openable.model();
		final Trap trap = model.lock().trap().get();
		if(!actor.hidden().contains(trap)) throw ActionException.of("disarm.unknown.trap");
		if(!model.isTrapped()) throw ActionException.of("disarm.already.disarmed");

		// Create disarm induction
		final Skill skill = super.skill(actor);
		final Induction induction = () -> {
			// Check still trapped
			if(!model.isTrapped()) throw ActionException.of("disarm.not.trapped");

			// Check whether successfully disarmed
			final boolean success = super.isSuccess(actor, skill, trap.difficulty());
			if(success) {
				model.disarm();
				return Response.of(TextHelper.join("trap.disarm.success"));
			}
			else {
				return Response.of(TextHelper.join("trap.disarm.failed"));
			}
		};

		// Build response
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
