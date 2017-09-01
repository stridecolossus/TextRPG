package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.Effect;

/**
 * Liquid descriptor.
 * @author Sarge
 */
public final class Liquid {
	/**
	 * Drinkable water.
	 */
	public static final Liquid WATER = new Liquid("water", 0, Effect.NONE);

	/**
	 * Oil for lanterns.
	 */
	public static final Liquid OIL = new Liquid("oil", 0, Effect.NONE);

	private final String name;
	private final int alcohol;
	private final Effect.Descriptor effect;

	/**
	 * Constructor.
	 * @param name			Liquid name
	 * @param alcohol		Alcohol level
	 * @param effect		Optional effect is this liquid can be drunk
	 */
	public Liquid(String name, int alcohol, Effect.Descriptor effect) {
		Check.notEmpty(name);
		Check.notNull(effect);
		this.name = name;
		this.alcohol = alcohol;
		this.effect = effect;
	}

	/**
	 * @return Liquid name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Alcohol level
	 */
	public int alcohol() {
		return alcohol;
	}

	/**
	 * @return Whether this liquid can be drunk
	 */
	public boolean isDrinkable() {
		return this != OIL;
	}

	/**
	 * @return Effect(s) if this liquid can be drunk
	 */
	public Effect.Descriptor effects() {
		return effect;
	}

	@Override
	public String toString() {
		return name;
	}
}
