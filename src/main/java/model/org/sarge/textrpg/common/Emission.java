package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import java.util.Comparator;
import java.util.function.Predicate;

import org.sarge.lib.object.EqualsBuilder;
import org.sarge.lib.object.ToString;
import org.sarge.textrpg.util.Percentile;

/**
 * Descriptor for an emission from an object.
 * @author Sarge
 * TODO - what is the name o the emission actually used for? only referenced in tests!) should it be a synthetic object (and therefore has a description)?
 */
public final class Emission {
	/**
	 * Intensity comparator.
	 */
	public static final Comparator<Emission> INTENSITY_COMPARATOR = Comparator.comparing(Emission::intensity);

	/**
	 * Light emission predicate.
	 */
	public static final Predicate<Emission> LIGHT_PREDICATE = Emission.Type.LIGHT.predicate();

	/**
	 * Types of emission.
	 */
	public static enum Type {
		LIGHT,
		SOUND,
		SMOKE;

		/**
		 * @return Emission predicate for this type of emission
		 */
		public Predicate<Emission> predicate() {
			return e -> e.type == this;
		}
	}

	private final Type type;
	private final Percentile intensity;

    /**
     * Constructor.
     * @param type          Type of emission
     * @param intensity     Intensity of this emission
     */
    public Emission(Type type, Percentile intensity) {
        this.type = notNull(type);
        this.intensity = notNull(intensity);
    }

	/**
	 * @return Type of emission
	 */
	public Type type() {
		return type;
	}

	/**
	 * @return Emission intensity
	 */
	public Percentile intensity() {
		return intensity;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.equals(this, obj);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
