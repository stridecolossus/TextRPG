package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Optional;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.common.Trap;
import org.sarge.textrpg.entity.EffectController;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to pick a lock.
 * @author Sarge
 */
@Component
public class PickLockAction extends SkillAction {
	private final EffectController effects;

	/**
	 * Constructor.
	 * @param skill Pick-lock skill
	 */
	public PickLockAction(@Value("#{skills.get('pick')}") Skill skill, EffectController effects) {
		super(skill, Flag.INDUCTION);
		this.effects = notNull(effects);
	}

	/**
	 * Pick a lock.
	 * @param actor				Actor
	 * @param lockpicks			Lockpicks
	 * @param openable			Openable to pick
	 * @return Response
	 * @throws ActionException if the given openable cannot be picked
	 * TODO - enable/re-lock action
	 */
	@RequiresActor
	public Response pick(Entity actor, @RequiredObject("lockpicks") DurableObject lockpicks, Openable openable) throws ActionException {
		// Check can be picked
		final var model = openable.model();
		if(model.lock() == Openable.Lock.LATCH) throw ActionException.of("pick.invalid.latch");
		if(!model.isLockable() || (model.state() != Openable.State.LOCKED)) throw ActionException.of("pick.not.locked");

		// TODO
		// - clicking noise emission during induction, repeating?
		// - level ~ skill/difficulty
		// - how to broadcast to other side if portal?

		// Create induction
		final Skill skill = super.skill(actor);
		final Induction induction = () -> {
			// Check whether unlocked during induction
			if(model.state() != Openable.State.LOCKED) throw ActionException.of("pick.already.unlocked");

			// Calculate pick-lock difficulty
			final Percentile diff = model.lock().difficulty();
			// TODO - lockpicks bonus

			// Determine outcome
			if(super.isSuccess(actor, skill, diff)) {
				model.pick();
				return Response.of("pick.success");
			}
			else {
				final Optional<Trap> trap = model.lock().trap();
				if(trap.isPresent()) {
					effects.apply(actor, trap.get().effects(), List.of(actor));
					return Response.of("pick.trap.activated");
				}
				else {
					return Response.of("pick.failed");
				}
			}
		};

		// Build response
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
