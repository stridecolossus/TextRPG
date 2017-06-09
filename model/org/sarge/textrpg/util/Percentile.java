package org.sarge.textrpg.util;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.EqualsBuilder;

/**
 * Percentile value.
 * @author Sarge
 */
public final class Percentile extends Number implements Comparable<Percentile> {
	/**
	 * 100% percentile.
	 */
	public static final Percentile ONE = new Percentile(100);

	/**
	 * 50% percentile.
	 */
	public static final Percentile HALF = new Percentile(50);

	/**
	 * 0% percentile.
	 */
	public static final Percentile ZERO = new Percentile(0);
	
	/**
	 * Converter for percentile values represented as a 0..1 floating-point or 0..100 integer number.
	 */
	public static final Converter<Percentile> CONVERTER = str -> {
		if(str.indexOf('.') == -1 ) {
			return new Percentile(Converter.INTEGER.convert(str));
		}
		else {
			return new Percentile(Converter.FLOAT.convert(str));
		}
	};

	private final float value;

	/**
	 * Constructor given a floating-point percentile.
	 * @param value Percentile represented as a 0..1 floating-point value
	 */
	public Percentile(float value) {
		Check.range(value, 0, 1);
		this.value = value;
	}

	/**
	 * Constructor given an integer percentile.
	 * @param value Percentile represented as a 0..100 integer value
	 */
	public Percentile(int value) {
		this(value / 100f);
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
		return (int) (value * 100);
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
	 * @return Invert this percentile
	 */
	public Percentile invert() {
		return new Percentile(1f - value);
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
		return this.isLessThan(that) ? -1 : +1;
	}
	
	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.equals(this, that);
	}
	
	@Override
	public String toString() {
		return intValue() + "%";
	}
}
