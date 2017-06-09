package org.sarge.textrpg.common;

import java.util.Collections;
import java.util.List;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictList;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.entity.EntityValueCalculator;
import org.sarge.textrpg.entity.MovementController;
import org.sarge.textrpg.object.PostManager;

/**
 * Context for an action.
 * @author Sarge
 */
public class ActionContext {
	private final List<EventQueue> queues = Collections.synchronizedList(new StrictList<>());
	
	private final TimeCycle cycle;
	private final EntityValueCalculator calc;
	private final MovementController mover;
	private final PostManager post;
	
	private long time = System.currentTimeMillis();
	
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
		add(EventQueue.GLOBAL);
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

	/**
	 * Adds an event queue.
	 * @param queue Event queue
	 */
	public void add(EventQueue queue) {
		queues.add(queue);
		queue.update(time);
	}

	/**
	 * Removes an event queue.
	 * @param queue Event queue
	 */
	public void remove(EventQueue queue) {
		queues.remove(queue);
	}

	/**
	 * Updates to the given time.
	 * @param time Current time
	 */
	public void update(long time) {
		this.time = time;
		queues.forEach(q -> q.update(time));
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
