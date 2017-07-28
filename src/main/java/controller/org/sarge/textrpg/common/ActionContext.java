package org.sarge.textrpg.common;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.entity.EntityValueCalculator;
import org.sarge.textrpg.entity.MovementController;
import org.sarge.textrpg.object.PostManager;

/**
 * Context for an action.
 * @author Sarge
 */
public class ActionContext {
	private final TimeCycle cycle;
	private final EntityValueCalculator calc;
	private final MovementController mover;
	private final PostManager post;

	private final long time = System.currentTimeMillis();

	/**
	 * Constructor.
	 * @param cycle		Time-cycle
	 * @param calc		Entity-value calculator
	 * @param mover		Movement controller
	 * @param post		Post manager
	 */
	public ActionContext(TimeCycle cycle, EntityValueCalculator calc, MovementController mover, PostManager post) {
		Check.notNull(cycle);
		Check.notNull(calc);
		Check.notNull(mover);
		Check.notNull(post);
		this.cycle = cycle;
		this.calc = calc;
		this.mover = mover;
		this.post = post;
	}

	/**
	 * @return Current system time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return Time-cycle
	 */
	public TimeCycle getTimeCycle() {
		return cycle;
	}

	/**
	 * @param hour Current hour
	 * @return Whether is day-light at the given hour
	 * TODO - move to seasonal time-cycle
	 */
	public boolean isDaylight() {
		final int hour = Clock.CLOCK.getHour();
		return (hour > 5) && (hour < 21);
	}

	/**
	 * @return Entity-value calculator
	 */
	public EntityValueCalculator getEntityValueCalculator() {
		return calc;
	}

	/**
	 * @return Movement controller
	 */
	public MovementController getMovementController() {
		return mover;
	}

	/**
	 * @return Post manager
	 */
	public PostManager getPostManager() {
		return post;
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
