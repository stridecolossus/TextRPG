package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.function.Function;

import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.world.MovementController.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Movement requirement that determines the base stamina cost.
 * @author Sarge
 */
@Component
public class DefaultMovementRequirement implements MovementController.Requirement {
	private Function<Terrain, Integer> terrain = terrain -> 1;
	private Function<Route, Float> route = route -> 1f;
	private float mod = 1;
	private float sneak = 1;

	/**
	 * Sets the terrain movement cost modifier.
	 * @param terrain Terrain modifier
	 */
	@Autowired
	public void setTerrainModifier(@Value("#{terrain.function('move', T(org.sarge.lib.util.Converter).INTEGER)}") Function<Terrain, Integer> terrain) {
		this.terrain = notNull(terrain);
	}

	/**
	 * Sets the route movement cost modifier.
	 * @param route Route modifier
	 */
	@Autowired
	public void setRouteModifier(@Value("#{route.function('move', T(org.sarge.lib.util.Converter).FLOAT)}") Function<Route, Float> route) {
		this.route = notNull(route);
	}

	/**
	 * Sets the overall movement cost multiplier.
	 * @param mod Multiplier
	 */
	@Autowired
	public void setModifier(@Value("${movement.multiplier}") float mod) {
		this.mod = oneOrMore(mod);
	}

	/**
	 * Sets the movement cost modifier when sneaking.
	 * @param sneak Sneak modifier
	 */
	@Autowired
	public void setSneakModifier(@Value("${movement.sneak.modifier}") float sneak) {
		this.sneak = oneOrMore(sneak);
	}

	@Override
	public Result result(Entity actor, Exit exit) {
		final float cost = cost(actor, exit);
		return new Result(null, (int) cost);
	}

	/**
	 * Calculates the overall movement cost modifier.
	 * @param actor		Actor
	 * @param mod		Action modifier
	 * @return Movement cost modifier
	 */
	public float cost(Entity actor, Exit exit) {
		// Apply terrain and route modifier
		final Location loc = actor.location();
		final float t = terrain.apply(loc.terrain());
		final float r = route.apply(exit.link().route());
		final float total = mod * t * r;

		// Apply stance modifier
		if(actor.model().stance() == Stance.SNEAKING) {
			return total * sneak;
		}
		else {
			return total;
		}
	}
}
