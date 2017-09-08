package org.sarge.textrpg.object;

import java.util.Collections;
import java.util.Set;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Emission;

/**
 * Material descriptor.
 * @author Sarge
 */
public final class Material {
	/**
	 * Default material.
	 */
	public static final Material DEFAULT = new Material("default", Collections.emptySet(), Collections.emptySet(), 0);

	private final String name;
	private final Set<Emission.Type> transparent;
	private final Set<DamageType> sustains;
	private final int str;

	/**
	 * Constructor.
	 * @param name				Material name
	 * @param transparent		Emission types that this material is transparent to, e.g. glass is transparent to {@link Emission.Type#LIGHT}
	 * @param sustains			Damage type that this material sustains, e.g. wood is damaged by {@link DamageType#FIRE}
	 * @param str				Strength of this material if it can be damaged
	 * @throws IllegalArgumentException if this material has strength but cannot be damaged
	 */
	public Material(String name, Set<Emission.Type> transparent, Set<DamageType> sustains, int str) {
		Check.notEmpty(name);
		Check.notNull(transparent);
		Check.notNull(sustains);
		Check.zeroOrMore(str);
		if((str > 0) && sustains.isEmpty()) throw new IllegalArgumentException("Strength must be zero for a material that cannot be damaged");
		this.name = name;
		this.transparent = transparent;
		this.sustains = sustains;
		this.str = str;
	}

	/**
	 * @return Name of this material
	 */
	public String name() {
		return name;
	}

    /**
     * @return Strength of this material (if it can be damaged)
     */
    public int strength() {
        return str;
    }

	/**
	 * @param type Emission type
	 * @return Whether this material is transparent to the given type of emission
	 */
	public boolean isTransparentTo(Emission.Type type) {
		return transparent.contains(type);
	}

	/**
	 * @param type Damage type
	 * @return Whether this material is damaged by the given type of damage
	 */
	public boolean isDamagedBy(DamageType type) {
		return sustains.contains(type);
	}

	@Override
	public String toString() {
		return name;
	}
}
