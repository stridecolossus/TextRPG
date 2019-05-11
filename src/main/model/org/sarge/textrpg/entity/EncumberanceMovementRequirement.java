package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.MovementController;
import org.sarge.textrpg.world.MovementController.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Encumberance movement listener.
 * @author Sarge
 */
@Component
public class EncumberanceMovementRequirement extends AbstractObject implements MovementController.Requirement {
	/**
	 * Encumbered result.
	 */
	public static final Result ENCUMBERED = new Result("move.encumbered");

	private final EncumberanceCalculator calc;

	private float mod = 1;

	/**
	 * Constructor.
	 * @param calc Encumberance calculator
	 */
	public EncumberanceMovementRequirement(EncumberanceCalculator calc) {
		this.calc = notNull(calc);
	}

	/**
	 * Sets the encumberance movement cost multiplier.
	 * @param mod Modifier
	 */
	@Autowired
	public void setEncumberanceModifier(@Value("${movement.encumberance.modifier}") float mod) {
		this.mod = oneOrMore(mod);
	}

	@Override
	public Result result(Entity actor, Exit exit) {
		final Percentile encumberance = calc.calculate(actor);
		if(encumberance.equals(Percentile.ONE)) {
			// Cannot move
			return ENCUMBERED;
		}
		else
		if(encumberance.equals(Percentile.ZERO)) {
			// Not encumbered
			return Result.DEFAULT;
		}
		else {
			// Partially encumbered
			final float cost = encumberance.floatValue() * mod;
			return new Result(null, (int) cost);
		}
	}
}
