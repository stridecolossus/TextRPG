package org.sarge.textrpg.common;

import java.util.Collection;
import java.util.function.IntBinaryOperator;

import org.sarge.textrpg.util.Randomiser;

/**
 * Integer value.
 * @author Sarge
 */
public interface Value {
	/**
	 * Evaluates this value for the given actor.
	 * @param actor Actor
	 * @return Integer value
	 */
	int evaluate(Actor actor);

	/**
	 * Zero value.
	 */
	Value ZERO = actor -> 0;
	
	/**
	 * One value.
	 */
	Value ONE = actor -> 1;

	/**
	 * Creates a literal integer value.
	 * @param value Literal
	 * @return Literal value
	 */
	static Value literal(int value) {
		return actor -> value;
	}
	
	/**
	 * Generates a random integer value in the given range.
	 * @param range Range
	 * @return Random value
	 * @see Randomiser#range(int)
	 */
	static Value random(int range) {
		return actor -> Randomiser.range(range);
	}

	/**
	 * Compound value operators.
	 */
	enum Operator {
		ADD(Integer::sum),
		MULTIPLY((a, b) -> a * b);
		
		private final IntBinaryOperator op;
		
		private Operator(IntBinaryOperator op) {
			this.op = op;
		}
	}

	/**
	 * Creates a compound value with the given operator.
	 * @param op			Operator
	 * @param values		Values
	 * @return Compound value
	 */
	static Value compound(Operator op, Collection<Value> values) {
		return actor -> values.stream().mapToInt(val -> val.evaluate(actor)).reduce(0, op.op);
	}
}
