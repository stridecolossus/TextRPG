package org.sarge.textrpg.common;

import java.util.function.Predicate;

import org.sarge.textrpg.entity.XXXCopyOfEntityManager.EntityState;

/**
 * Actor performing an {@link EntityState}.
 * @author Sarge
 */
public interface Actor extends Parent {
	/**
	 * @param obj Partially hidden object
	 * @return Whether this actor perceives the given object
	 */
	boolean perceives(Hidden obj);

	/**
	 * @return Whether this actor is a player-character
	 */
	default boolean isPlayer() {
		return false;
	}

	/**
	 * @return Size of this actor
	 */
	Size getSize();

	/**
	 * @return Event queue for this actor.
	 */
	EventQueue getEventQueue();

	/**
	 * Alerts this actor.
	 * @param n Notification
	 */
	void alert(Notification n);

	/**
	 * Actor for anonymous scripts, e.g. reset events.
	 */
	Actor SYSTEM = new Actor() {
		@Override
		public Contents getContents() {
			return Contents.IMMUTABLE;
		}

		@Override
		public Parent getParent() {
			return null;
		}

		@Override
		public EventQueue getEventQueue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean perceives(Hidden obj) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Size getSize() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void alert(Notification n) {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * Creates a filter that ignores the given actor.
	 * @param actor Actor
	 * @return Filter
	 */
	static Predicate<Thing> filter(Actor actor) {
		return t -> t != actor;
	}
}
