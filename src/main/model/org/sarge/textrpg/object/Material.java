package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Set;

import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Emission;

/**
 * Descriptor for the material properties of an object.
 * @author Sarge
 */
public final class Material extends AbstractEqualsObject {
	/**
	 * Default material.
	 */
	public static final Material NONE = new Material.Builder("material.none").build();

	private final String name;
	private final int strength;
	private final Set<Damage.Type> damaged;
	private final Set<Emission> transparent;
	private final boolean floats;

	/**
	 * Constructor.
	 * @param name				Material name
	 * @param strength			Strength
	 * @param damaged			Damage-types that affect this material
	 * @param transparent		Emission that this material is transparent to, e.g. glass is transparent to {@link Emission.Format#STARTLED}
	 * @param floats			Whether an object of this material will float in water
	 */
	private Material(String name, int strength, Set<Damage.Type> damaged, Set<Emission> transparent, boolean floats) {
		if(damaged.isEmpty() != (strength == 0)) throw new IllegalArgumentException("Materials that can be damaged must have some strength");
		this.name = notEmpty(name);
		this.strength = zeroOrMore(strength);
		this.damaged = Set.copyOf(damaged);
		this.transparent = Set.copyOf(transparent);
		this.floats = floats;
	}

	/**
	 * @return Name of this material
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Strength of this material
	 */
	public int strength() {
		return strength;
	}

	/**
	 * @param type Damage-type
	 * @return Whether this material is damaged by the given damage-type
	 */
	public boolean isDamagedBy(Damage.Type type) {
		return damaged.contains(type);
	}

	/**
	 * @param emission Emission type
	 * @return Whether this material is transparent to the given emission
	 */
	public boolean isTransparentTo(Emission emission) {
		return transparent.contains(emission);
	}

	/**
	 * @return Whether an object of this material will float in water
	 */
	public boolean isFloating() {
		return floats;
	}

	/**
	 * Builder for a material.
	 */
	public static class Builder {
		private final String name;
		private int strength;
		private final Set<Damage.Type> damaged = new StrictSet<>();
		private final Set<Emission> transparent = new StrictSet<>();
		private boolean floats;

		/**
		 * Constructor.
		 * @param name Material name
		 */
		public Builder(String name) {
			this.name = name;
		}

		/**
		 * Sets the strength of this material.
		 * @param strength Strength
		 */
		public Builder strength(int strength) {
			this.strength = strength;
			return this;
		}

		/**
		 * Adds a damage-type that this material is damaged by.
		 * @param type Damage-type
		 */
		public Builder damaged(Damage.Type type) {
			damaged.add(type);
			return this;
		}

		/**
		 * Adds an emission type that this material is transparent to.
		 * @param emission Emission type
		 */
		public Builder transparent(Emission emission) {
			transparent.add(emission);
			return this;
		}

		/**
		 * Sets this material as floating in water.
		 */
		public Builder floats() {
			this.floats = true;
			return this;
		}

		/**
		 * Constructs this material.
		 * @return New material
		 */
		public Material build() {
			return new Material(name, strength, damaged, transparent, floats);
		}
	}
}
