package org.sarge.textrpg.common;

import java.util.List;

import org.sarge.lib.util.Check;

/**
 * Condition on an {@link Actor}.
 * @author Sarge
 */
public interface Condition {
	/**
	 * Evaluates this condition.
	 * @param actor Actor
	 * @return Whether this condition is satisfied by the given actor
	 */
	boolean evaluate(Actor actor);
	
	/**
	 * Condition that is always <tt>true</tt>.
	 */
	Condition TRUE = actor -> true;

	/**
	 * Creates a compound condition.
	 * @param conditions Conditions
	 * @return Compound condition
	 */
	static Condition compound(List<Condition> conditions) {
		Check.notEmpty(conditions);
		return actor -> conditions.stream().allMatch(c -> c.evaluate(actor));
	}

	/**
	 * Creates an inverse condition.
	 * @param c Condition
	 * @return Inverted condition
	 */
	static Condition invert(Condition c) {
		return actor -> !c.evaluate(actor);
	}
}
