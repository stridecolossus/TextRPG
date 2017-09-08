package org.sarge.textrpg.common;

import java.util.function.Predicate;

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
	Size size();

	/**
	 * @return Event queue for this actor.
	 */
	EventQueue queue();

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
		public Contents contents() {
			return Contents.EMPTY;
		}

		@Override
		public Parent parent() {
			return null;
		}

		@Override
		public EventQueue queue() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean perceives(Hidden obj) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Size size() {
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
