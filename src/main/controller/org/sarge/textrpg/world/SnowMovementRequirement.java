package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.range;

import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.MovementController.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Movement requirement based on the level of snow in the <b>destination</b> location.
 * @author Sarge
 * @see Weather#snow()
 */
@Component
public class SnowMovementRequirement implements MovementController.Requirement {
	private final SnowModel model;

	private int threshold;
	private int max = Integer.MAX_VALUE;
	private float mod = 1;

	/**
	 * Constructor.
	 * @param model Snow level model
	 */
	public SnowMovementRequirement(SnowModel model) {
		this.model = notNull(model);
	}

	/**
	 * Sets the threshold for applying the snow modifier.
	 * @param threshold Snow threshold
	 */
	@Autowired
	public void setSnowThreshold(@Value("${movement.snow.threshold}") int threshold) {
		this.threshold = range(threshold, 0, max);
	}

	/**
	 * Sets the maximum snow level that can be traversed.
	 * @param max Maximum snow level
	 */
	@Autowired
	public void setMaxSnowLevel(@Value("${movement.snow.max.level}") int max) {
		if(max < threshold) throw new IllegalArgumentException("Maximum snow level cannot be less-than the modifier threshold");
		this.max = oneOrMore(max);
	}

	/**
	 * Sets the movement cost modifier for snow.
	 * @param mod Snow modifier
	 */
	@Autowired
	public void setSnowModifier(@Value("${movement.snow.modifier}") float mod) {
		this.mod = oneOrMore(mod);
	}

	@Override
	public MovementController.Result result(Entity actor, Exit exit) {
		final int snow = snow(exit.destination());
		if(snow > 0) {
			if(snow < max) {
				// Snow present
				final Description description = new Description.Builder("move.snow.level").build();
				return new Result(description, (int) (snow * mod));
			}
			else {
				// Snow too deep
				return new Result("move.too.deep");
			}
		}
		else {
			// No snow at destination
			return MovementController.Result.DEFAULT;
		}
	}

	/**
	 * Determines the snow level at the given location.
	 * @param loc Location
	 * @return Snow level
	 * @see SnowModel#snow(Location)
	 */
	private int snow(Location loc) {
		if(loc.terrain().isSnowSurface()) {
			final int level = model.snow(loc);
			if(level < threshold) {
				// Snow level ignored
				return 0;
			}
			else {
				// Snow present
				return level;
			}
		}
		else {
			// No snow in location
			return 0;
		}
	}
}
