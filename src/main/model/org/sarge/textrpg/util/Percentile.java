package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.range;

import java.util.Comparator;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;

/**
 * A <i>percentile</i> represents a percentage as a 0..1 bounded floating-point value.
 * @author Sarge
 */
public final class Percentile extends Number implements Comparable<Percentile> {
	private static final float ACCURACY = 0.0001f;

	/**
	 * Maximum integer percentile value.
	 */
	public static final int MAX = 100;

	/**
	 * Cache of 0..100 integer percentiles.
	 */
	private static final Percentile[] CACHE = new Percentile[MAX + 1];

	static {
		for(int n = 0; n <= MAX; ++n) {
			CACHE[n] = new Percentile(n);
		}
	}

	/**
	 * 0% percentile.
	 */
	public static final Percentile ZERO = Percentile.of(0);

	/**
	 * 50% percentile.
	 */
	public static final Percentile HALF = Percentile.of(50);

	/**
	 * 100% percentile.
	 */
	public static final Percentile ONE = Percentile.of(MAX);

	/**
	 * Percentile comparator.
	 */
	public static final Comparator<Percentile> COMPARATOR = Comparator.comparing(Percentile::floatValue);

	/**
	 * Converter for percentile values represented as a 0..1 floating-point <b>or</b> a 0..100 integer number.
	 */
	public static final Converter<Percentile> CONVERTER = str -> {
		if(str.indexOf('.') == -1) {
			return Percentile.of(Converter.INTEGER.apply(str));
		}
		else {
			return new Percentile(Converter.FLOAT.apply(str));
		}
	};

	/**
	 * Creates and caches an integer percentile.
	 * @param value Value 0..100
	 * @return Percentile
	 * @throws ArrayIndexOutOfBoundsException if the given value is not a valid integer percentile
	 */
	public static Percentile of(int value) {
		return CACHE[value];
	}

	/**
	 * Creates an integer percentile in the given range.
	 * @param value Value
	 * @param range	Range
	 * @return Percentile
	 * @see #of(int)
	 */
	public static Percentile of(int value, int range) {
		Check.range(value, 0, range);
		if(value == 0) {
			return Percentile.ZERO;
		}
		else
		if(value == range) {
			return Percentile.ONE;
		}
		else {
			return Percentile.of(value * MAX / range);
		}
	}

	private final float value;

	/**
	 * Constructor given a floating-point percentile.
	 * @param value Percentile represented as a 0..1 floating-point value
	 * @throws IllegalArgumentException if the given value is not a valid percentile
	 */
	public Percentile(float value) {
		this.value = range(value, 0f, 1f);
	}

	/**
	 * Constructor given an integer percentile.
	 * @param value Percentile represented as a 0..100 integer value
	 * @throws IllegalArgumentException if the given value is not a valid integer percentile
	 */
	private Percentile(int value) {
		this(value / (float) MAX);
	}

	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	public float floatValue() {
		return value;
	}

	@Override
	public int intValue() {
		return (int) (value * MAX);
	}

	@Override
	public long longValue() {
		return intValue();
	}

	@Override
	public short shortValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte byteValue() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return Inverts this percentile
	 */
	public Percentile invert() {
		return new Percentile(1f - value);
	}

	/**
	 * Multiplies this percentile by the given value.
	 * @param scale Scale
	 * @return Scaled percentile
	 */
	public Percentile scale(Percentile scale) {
		return new Percentile(value * scale.value);
	}

	/**
	 * @return Whether this percentile is zero
	 */
	public boolean isZero() {
		return Math.abs(value) < ACCURACY;
	}

	/**
	 * Comparator.
	 * @param p Percentile
	 * @return Whether this percentile is less-than the given value
	 */
	public boolean isLessThan(Percentile p) {
		return this.value < p.value;
	}

	@Override
	public int compareTo(Percentile that) {
		if(this.isLessThan(that)) {
			return -1;
		}
		else
		if(that.isLessThan(this)) {
			return +1;
		}
		else {
			return 0;
		}
	}

	/**
	 * Minimum operator.
	 * @param that Percentile
	 * @return Minimum of this and the given percentile
	 */
	public Percentile min(Percentile that) {
		return isLessThan(that) ? this : that;
	}

	/**
	 * Maximum operator.
	 * @param that Percentile
	 * @return Maximum of this and the given percentile
	 */
	public Percentile max(Percentile that) {
		return isLessThan(that) ? that : this;
	}

	@Override
	public int hashCode() {
		return Float.hashCode(value);
	}

	@Override
	public boolean equals(Object that) {
		if(this == that) return true;
		if(that == null) return false;
		if(that instanceof Percentile) {
			final Percentile p = (Percentile) that;
			return Math.abs(this.value - p.value) < ACCURACY;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return intValue() + "%";
	}
}
