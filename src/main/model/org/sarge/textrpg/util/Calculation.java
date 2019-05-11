package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

/**
 * A <i>calculation</i> is a floating-point value evaluated with respect to a given entity.
 * @author Sarge
 * @see ValueModifier
 */
@FunctionalInterface
public interface Calculation {
	/**
	 * Evaluates this value.
	 * @param actor Actor
	 * @return Integer value
	 */
	double evaluate(ValueModifier.Source src);

	/**
	 * Zero value.
	 */
	Calculation ZERO = literal(0);

	/**
	 * Creates a literal integer value.
	 * @param value Literal
	 * @return Literal value
	 */
	static Calculation literal(int value) {
		return ignore -> value;
	}

	/**
	 * Creates a randomised integer value.
	 * @param base		Base value
	 * @param range		Range
	 * @return Random value
	 * @see Randomiser#range(int)
	 */
	static Calculation random(int base, int range) {
		if((base == 0) && (range < 1)) throw new IllegalArgumentException("Invalid range");
		return ignore -> base + Randomiser.range(range);
	}

	/**
	 * Creates a scaled value.
	 * @param delegate		Delegate
	 * @param scale			Scale
	 * @return Scaled value
	 */
	static Calculation scaled(Calculation delegate, float scale) {
		return src -> delegate.evaluate(src) * scale;
	}

	/**
	 * Creates a percentile value.
	 * @param key Key
	 * @return Percentile value
	 */
	static Calculation percentile(Calculation delegate) {
		// TODO - verify is percentile
		return src -> (Percentile.MAX - delegate.evaluate(src)) / Percentile.MAX;
	}

	/**
	 * Compound value accumulator operation.
	 */
	enum Operator {
		SUM(0, Double::sum),
		MULTIPLY(1, (a, b) -> a * b);

		private final int identity;
		private final DoubleBinaryOperator op;

		private Operator(int identity, DoubleBinaryOperator op) {
			this.identity = identity;
			this.op = notNull(op);
		}
	}

	/**
	 * Creates a compound integer value.
	 * @param values	Values
	 * @param op		Accumulator operation
	 * @return Compound value
	 */
	static Calculation compound(List<Calculation> values, Operator op) {
		return src -> values.stream().mapToDouble(value -> value.evaluate(src)).reduce(op.identity, op.op);
	}
}
