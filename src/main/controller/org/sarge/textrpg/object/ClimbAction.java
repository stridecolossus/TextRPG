package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.entity.Transaction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.world.DefaultMovementRequirement;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.MovementController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to climb something.
 * @author Sarge
 * @see ClimbLink
 * @see PlayerSettings.Setting#CLIMB_SAFE
 */
@Component
public class ClimbAction extends SkillAction {
	private final MovementController mover;
	private final DefaultMovementRequirement calc;

	/**
	 * Constructor.
	 * @param mover		Movement controller
	 * @param calc		Movement cost calculator
	 * @param skill		Climb skill
	 */
	public ClimbAction(MovementController mover, DefaultMovementRequirement calc, @Value("#{skills.get('climb')}") Skill skill) {
		super(skill);
		this.mover = notNull(mover);
		this.calc = notNull(calc);
	}

	/**
	 * Attempts to climb the given object.
	 * @param actor		Actor
	 * @param obj		Object to climb
	 * @return Response
	 * @throws ActionException if the object cannot be climbed or the actor cannot climb
	 */
	@RequiresActor
	public Response climb(PlayerCharacter actor, WorldObject obj) throws ActionException {
		// Find associated link
		final Exit exit = actor.location().exits().stream()
			.filter(e -> e.isPerceivedBy(actor))
			.filter(e -> e.link() instanceof ClimbLink)
			.filter(e -> e.link().controller().get() == obj)
			.findAny()
			.orElseThrow(() -> ActionException.of("climb.invalid.object"));

		// Determine whether climb succeeds
		final ClimbLink link = (ClimbLink) exit.link();
		final Skill skill = super.skill(actor);
		final boolean success = super.isSuccess(actor, skill, link.difficulty());

		// Check whether will only attempt safe climbs
		if(!success) {
			final boolean safe = actor.settings().toBoolean(PlayerSettings.Setting.CLIMB_SAFE);
			if(safe) {
				throw ActionException.of("climb.not.safe");
			}
		}

		// Interrupt active induction
		interrupt(actor);

		// Attempt climb
		if(success) {
			// Traverse link
			mover.move(actor, exit, skill.scale()); // TODO - should also be * link.modifier()?
			return Response.DISPLAY_LOCATION;
		}
		else {
			// Consume stamina
			final var values = actor.model().values();
			final float cost = calc.cost(actor, exit) * link.modifier();
			final Transaction tx = values.transaction(EntityValue.STAMINA, (int) cost, "climb.insufficient.stamina");
			tx.check();			// TODO - should we just ignore this? climb is already failed
			tx.complete();

			// Apply falling damage
			// TODO

			// Fall to bottom of climb
			if(!link.up()) {
				actor.parent(exit.destination());
			}

			// Build response
			final Response.Builder response = new Response.Builder();
			if(values.get(EntityValue.STAMINA.key()).get() < 1) {
				response.add("move.exhausted"); // TODO - constant, factor out stamina test
			}
			// TODO - add damage description
			if(values.get(EntityValue.HEALTH.key()).get() < 1) {
				response.add("climb.fall.death");
			}
			return response.build();
		}
	}
}
