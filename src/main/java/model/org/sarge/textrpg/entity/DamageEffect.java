package org.sarge.textrpg.entity;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Value;

/**
 * Damage effect.
 * @author Sarge
 */
public class DamageEffect implements EffectMethod {
	private final DamageType type;
	private final Value amount;
	private final boolean wound;

	/**
	 * Constructor.
	 * @param type		Type of damage
	 * @param amount	Damage amount
	 * @param wound		Whether this effect is a wound (that can be bandaged)
	 */
	public DamageEffect(DamageType type, Value amount, boolean wound) {
		Check.notNull(type);
		Check.notNull(amount);
		this.type = type;
		this.amount = amount;
		this.wound = wound;
	}

	/**
	 * @return Damage-type
	 */
	public DamageType damageType() {
		return type;
	}

	/**
	 * @return Amount of damage
	 */
	public Value amount() {
		return amount;
	}

	@Override
	public boolean isWound() {
		return wound;
	}

	@Override
	public void apply(Entity e, int size) {
		assert size > 0;
		e.damage(type, size);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
