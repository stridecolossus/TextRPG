package org.sarge.textrpg.world;

import org.sarge.textrpg.entity.Boat;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.MovementController.Result;
import org.springframework.stereotype.Component;

/**
 * Movement requirement for water destinations.
 * @author Sarge
 * @see Location#isWater()
 */
@Component
public class WaterMovementRequirement implements MovementController.Requirement {
	private static final Result FROZEN = new Result(Description.of("move.frozen.water"), 0);
	private static final Result CANNOT_SWIM = new Result("move.cannot.swim");

	@Override
	public Result result(Entity actor, Exit exit) {
		final boolean water = exit.destination().isWater();
		if(water) {
			return result(actor);
		}
		else {
			return Result.DEFAULT;
		}
	}

	/**
	 * Determine the result of attempting to move on water.
	 * @param actor Actor
	 * @return Result
	 */
	private static Result result(Entity actor) {
		final boolean frozen = actor.location().isFrozen();
		if(frozen) {
			// Move on frozen water
			return FROZEN;
		}
		else
		if(actor.model().stance() == Stance.SWIMMING) {
			// Already swimming
			return Result.DEFAULT;
		}
		else
		if(actor.parent() instanceof Boat) {
			// Using boat
			return Result.DEFAULT;
		}
		else
		if(actor.isSwimEnabled()) {
			// Start swimming
			return Result.DEFAULT;
		}
		else {
			// Actor cannot swim
			return CANNOT_SWIM;
		}
	}
}
