package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.entity.Entity.LocationTrigger;
import org.sarge.textrpg.entity.EntityModel.AppliedEffect;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.world.Location;

/**
 * An <i>aura</i> applies transient effects to entities in a location.
 * @author Sarge
 */
public class AuraLocationTrigger extends AbstractObject implements LocationTrigger {
	private final EffectController controller;
	private final Effect effect;

	/**
	 * Constructor.
	 * @param effect 			Aura effect
	 * @param controller		Effect controller
	 */
	public AuraLocationTrigger(Effect effect, EffectController controller) {
		if(effect.duration().isZero()) throw new IllegalArgumentException("Aura effect must have a duration");
		this.effect = notNull(effect);
		this.controller = notNull(controller);
	}

	@Override
	public void trigger(Entity actor) {
		// Apply effect
		final AppliedEffect applied = controller.apply(effect, actor);

		// Register refresh event
		final Location start = actor.location();
		final Event refresh = () -> {
			// Stop if no longer in trigger location
			if(actor.location() != start) {
				applied.remove();
				return false;
			}

			// Increment effect
			if(effect.times() > 1) {
				// TODO
				// applied.increment()?
			}

			// Repeat to refresh
			return true;
		};
		actor.manager().queue().add(refresh, effect.duration());
	}
}
