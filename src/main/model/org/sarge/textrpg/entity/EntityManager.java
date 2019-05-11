package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Consumer;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Event.Queue;

/**
 * An <i>entity manager</i> aggregates the various aspects of entity management.
 * <p>
 * The manager is comprised of:
 * <ul>
 * <li>an event queue for events pertaining to the entity</li>
 * <li>a manager for the current induction</li>
 * <li>a handler for notifications</li>
 * <li>the latest time the entity was updated</li>
 * </ul>
 * @author Sarge
 */
public class EntityManager extends AbstractObject {
	private final Event.Queue queue;
	private final Induction.Manager induction;
	private final Notification.Handler handler;

	private long updated;

	/**
	 * Constructor.
	 * @param queue			Event queue
	 * @param handler		Notification handler
	 * @param listener		Listener for induction responses
	 */
	protected EntityManager(Queue queue, Notification.Handler handler, Consumer<Response> listener) {
		this.queue = notNull(queue);
		this.induction = new Induction.Manager(queue, listener);
		this.handler = notNull(handler);
	}

	/**
	 * @return Entity event queue
	 */
	public Event.Queue queue() {
		return queue;
	}

	/**
	 * @return Induction manager
	 */
	public Induction.Manager induction() {
		return induction;
	}

	/**
	 * @return Notification handler
	 */
	public Notification.Handler handler() {
		return handler;
	}

	/**
	 * @return Latest update time
	 * @see #update(long)
	 */
	public long updated() {
		return updated;
	}

	/**
	 * Updates this <i>latest update</i> time for this entity.
	 * @param now Current time
	 * @throws IllegalStateException if the update time is not later than the latest update-time
	 * @see #updated()
	 */
	void update(long now) {
		if(now < updated) throw new IllegalStateException("Invalid update time");
		updated = now;
	}
}
