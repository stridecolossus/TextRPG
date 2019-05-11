package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractObject;
import org.sarge.lib.util.Converter;
import org.sarge.textrpg.util.Calculation;

/**
 * Damage descriptor.
 * @author Sarge
 */
public final class Damage extends AbstractObject {
	/**
	 * Default damage.
	 */
	public static final Damage DEFAULT = new Damage(Type.GENERAL, Calculation.literal(1), Effect.NONE);

	/**
	 * Damage types.
	 */
	public enum Type implements CommandArgument {
		GENERAL,
		PIERCING,
		SLASHING,
		CRUSHING,
		FIRE,
		COLD,
		FEAR,
		POISON;

		/**
		 * Damage-type converter.
		 */
		public static final Converter<Type> CONVERTER = Converter.enumeration(Type.class);

		/**
		 * @return Whether a weapon of this damage-type can be whetted
		 */
		public boolean isWhetWeapon() {
			switch(this) {
			case PIERCING:
			case SLASHING:
				return true;

			default:
				return false;
			}
		}
	}

	private final Type type;
	private final Calculation amount;
	private final Effect effect;

	/**
	 * Constructor.
	 * @param type			Damage type
	 * @param amount		Amount of damage
	 * @param effect		Additional effect(s)
	 */
	public Damage(Type type, Calculation amount, Effect effect) {
		this.type = notNull(type);
		this.amount = notNull(amount);
		this.effect = notNull(effect);
	}

	/**
	 * @return Damage type
	 */
	public Type type() {
		return type;
	}

	/**
	 * @return Amount of damage
	 */
	public Calculation amount() {
		return amount;
	}

	/**
	 * @return Additional effect(s)
	 */
	public Effect effect() {
		return effect;
	}
}
