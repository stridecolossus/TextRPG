package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.common.Trap;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to detect a {@link Trap}.
 * @author Sarge
 */
@Component
public class TrapDetectAction extends SkillAction {
	private final Duration forget;

	/**
	 * Constructor.
	 * @param skill 	Detect-trap skill
	 * @param forget	Forget duration
	 */
	public TrapDetectAction(@Value("#{skills.get('detect')}") Skill skill, @Value("${trap.forget}") Duration forget) {
		super(skill, Flag.LIGHT, Flag.INDUCTION);
		this.forget = notNull(forget);
	}

	/**
	 * Detects traps on the given openable.
	 * @param actor			Actor
	 * @param skill			Detect skill
	 * @param openable		Openable
	 * @return Response
	 */
	@RequiresActor
	public Response detect(PlayerCharacter actor, Openable openable) {
		// Create detect induction
		final Skill skill = super.skill(actor);
		final Induction induction = () -> {
			final var model = openable.model();
			if(model.isTrapped()) {
				final Trap trap = model.lock().trap().get();
				final boolean success = super.isSuccess(actor, skill, trap.difficulty());
				if(success) {
					actor.hidden().add(trap, forget);
				}
				return response(success);
			}
			else {
				return response(false);
			}
		};

		// Build response
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}

	/**
	 * Builds detect response.
	 */
	private static Response response(boolean success) {
		return Response.of(TextHelper.join("trap.detect", success ? "success" : "failed"));
	}
}
