package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.entity.Race.Behaviour;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Randomiser;
import org.springframework.stereotype.Component;

/**
 * Default AI notification handler.
 * @author Sarge
 */
@Component
public class DefaultNotificationHandler implements Notification.Handler {
	private final BehaviourController controller;

	/**
	 * Constructor.
	 * @param controller Behaviour controller
	 */
	public DefaultNotificationHandler(BehaviourController controller) {
		this.controller = notNull(controller);
	}

	@Override
	public void init(Entity actor) {
		controller.revert(actor);
	}

	@Override
	public void handle(MovementNotification move, Entity actor) {
		final Race.Behaviour behaviour = actor.descriptor().race().behaviour();
		if(move.isArrival()) {
			final boolean passed = moraleCheck(actor);
			if(behaviour.isFlag(Race.Behaviour.Flag.STARTLED) && !passed) {
				controller.flee(actor);
			}
			else
			if(passed) {
				controller.attack(actor, move.actor());
			}
		}
		else {
			// TODO - if combat AND move.actor == target AND follows THEN follow ELSE stop combat, rest if injured, or default
			// where 'follows' = not static and dest terrain
			// track?
		}
	}

	@Override
	public void handle(EmissionNotification emission, Entity actor) {
		// TODO
		// - investigate / ignore
	}

	@Override
	public void handle(CombatNotification combat, Entity actor) {
		if(!moraleCheck(actor)) {
			controller.flee(actor);
		}
	}

	@Override
	public void light(Entity actor) {
		final Race.Behaviour behaviour = actor.descriptor().race().behaviour();
		if(behaviour.isFlag(Behaviour.Flag.STARTLED) && !moraleCheck(actor)) {
			controller.flee(actor);
		}
	}

	/**
	 * Performs a morale check for the given actor.
	 * @param actor Actor
	 * @return Whether the given entity passes the morale check
	 */
	private static boolean moraleCheck(Entity actor) {
		final Race.Behaviour behaviour = actor.descriptor().race().behaviour();
		final Percentile aggression = behaviour.aggression();
		if(Percentile.ZERO.equals(aggression)) {
			return false;
		}
		else
		if(Percentile.ONE.equals(aggression)) {
			return true;
		}
		else {
			// TODO - factor in health and group casualties
			return Randomiser.isLessThan(aggression);
		}
	}
}
