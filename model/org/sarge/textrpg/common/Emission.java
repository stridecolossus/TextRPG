package org.sarge.textrpg.common;

import java.util.Comparator;
import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.ToString;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.util.Percentile;

/**
 * Descriptor for an emission from an object.
 * @author Sarge
 */
public final class Emission {
	/**
	 * Intensity comparator.
	 */
	public static final Comparator<Emission> INTENSITY_COMPARATOR = Comparator.comparing(Emission::getIntensity);

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
		ODOUR,
		SMOKE;

		/**
		 * @return Whether this type of emission has a name
		 */
		public boolean hasName() {
			switch(this) {
			case SOUND:
			case ODOUR:
				return true;
				
			default:
				return false;
			}
		}

		/**
		 * @return Emission predicate for this type of emission
		 */
		public Predicate<Emission> predicate() {
			return e -> e.type == this;
		}
	}
	
	/**
	 * Convenience factory for a light emission.
	 * @param intensity Light intensity
	 * @return Light emission
	 */
	public static Emission light(Percentile intensity) {
		return new Emission(null, Type.LIGHT, intensity);
	}
	
	private final String name;
	private final Type type;
	private final Percentile intensity;

	/**
	 * Constructor.
	 * @param name			Emission name
	 * @param type			Type of emission
	 * @param intensity		Intensity of this emission
	 */
	public Emission(String name, Type type, Percentile intensity) {
		Check.notNull(type);
		Check.notNull(intensity);
		if(Util.isEmpty(name) == type.hasName()) throw new IllegalArgumentException(String.format("Emission name mismatch: type=%s name=%s", type, name));
		this.name = name;
		this.type = type;
		this.intensity = intensity;
	}
	
	/**
	 * @return Emission name or <tt>null</tt> if none
	 * @see Type#hasName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return Type of emission
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return Emission intensity
	 */
	public Percentile getIntensity() {
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
