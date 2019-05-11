package org.sarge.textrpg.common;

import org.sarge.textrpg.util.Description;

/**
 * Condition to use a skill or piece of equipment.
 * @author Sarge
 */
public interface Condition {
	/**
	 * Condition that is always satisfied.
	 */
	Condition TRUE = new Condition() {
		@Override
		public boolean matches(Actor actor) {
			return true;
		}

		@Override
		public Description reason() {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * @param actor Actor
	 * @return Whether this condition is satisfied by the given actor
	 */
	boolean matches(Actor actor);

	/**
	 * @return Description of this condition
	 */
	Description reason();
}
