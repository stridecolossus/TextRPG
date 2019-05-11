package org.sarge.textrpg.entity;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to backstab an enemy.
 * @author Sarge
 */
@Component
public class BackStabAction extends SkillAction {
	/**
	 * Constructor.
	 * @param skill Back-stab skill
	 */
	public BackStabAction(@Value("#{skills.get('backstab')}") Skill skill) {
		super(skill, Flag.LIGHT, Flag.INDUCTION, Flag.REVEALS);
	}

	/**
	 * Back-stabs the given enemy.
	 * @param actor		Actor
	 * @param target	Target
	 * @return Response
	 * @throws ActionException if the actor is not sneaking/hiding, does not have a piercing weapon equipped, or the given entity is not a valid target
	 * @see Damage.Type#PIERCING
	 */
	@RequiresActor
	public Response backstab(Entity actor, Entity target) throws ActionException {
		// Check sneaking or hiding
		final var model = actor.model();
		if(!model.stance().isVisibilityModifier()) throw ActionException.of("backstab.requires.hidden");

		// Check valid target
		// TODO

		// Check suitable weapon equipped
		final Weapon weapon = actor.contents().equipment().weapon()
			.filter(w -> w.descriptor().damage().type() == Damage.Type.PIERCING)
			.filter(StreamUtil.not(Weapon::isBroken))
			.orElseThrow(() -> ActionException.of("backstab.requires.weapon"));

		// Calculate target difficulty
		// TODO
		final Skill skill = super.skill(actor);
		final Percentile diff = Percentile.HALF;

		// Create induction
		final Induction induction = () -> {
			// Stop sneaking/hiding
			model.stance(Stance.DEFAULT);
			model.values().visibility().remove();

			// Check target still available
			// TODO

			// Backstab
			final Response.Builder response = new Response.Builder();
			if(isSuccess(actor, skill, diff)) {
				// Apply damage
				final int mod = skill.scale();
				// TODO - apply damage, add to response
			}
			else {
				// Missed
				response.add("backstab.failed");
			}

			// Apply wear
			weapon.use();
			// TODO - check broken in combat controller

			// Start combat
			if(target.isAlive()) {
				// TODO
			}

			// Build response
			return response.build();
		};

		// Start backstab
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
