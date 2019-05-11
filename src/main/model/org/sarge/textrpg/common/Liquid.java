package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Descriptor for a liquid.
 * @author Sarge
 */
public final class Liquid extends AbstractEqualsObject {
	/**
	 * Water.
	 */
	public static final Liquid WATER = new Liquid("water");

	/**
	 * Oil.
	 */
	public static final Liquid OIL = new Liquid("oil");

	private final String name;
	private final Effect effect;
	private final Effect.Group curative;

	/**
	 * Constructor.
	 * @param name			Liquid name
	 * @param effect		Effect(s) when drunk
	 * @param curative		Curative effect type or {@link Effect.Group#DEFAULT} if not a curative
	 */
	public Liquid(String name, Effect effect, Effect.Group curative) {
		// TODO - why effect/curative args?
		this.name = notEmpty(name);
		this.effect = notNull(effect);
		this.curative = notNull(curative);
		if((effect == Effect.NONE) && (curative == Effect.Group.DEFAULT)) throw new IllegalArgumentException("Invalid liquid");
	}

	/**
	 * Constructor for a pre-defined liquid.
	 * @param name Liquid name
	 */
	private Liquid(String name) {
		this.name = notEmpty(name);
		this.effect = Effect.NONE;
		this.curative = Effect.Group.DEFAULT;
	}

	/**
	 * @return Name of this liquid
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Effect(s) of this liquid when drunk
	 */
	public Effect effect() {
		return effect;
	}

	/**
	 * @return Effect type for a curative potion
	 */
	public Effect.Group curative() {
		return curative;
	}
}
