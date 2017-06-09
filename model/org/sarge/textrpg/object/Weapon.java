package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.DamageEffect;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.object.DurableObject.Descriptor;

/**
 * Weapon descriptor.
 * @author Sarge
 */
public class Weapon extends Descriptor {
	private final int speed;
	private final DamageEffect damage;
	private final Optional<Effect.Descriptor> effect;
	private final Optional<String> ammo;

	/**
	 * Constructor.
	 * @param descriptor		Descriptor
	 * @param durability		Durability
	 * @param speed				Weapon speed
	 * @param damage			Damage effect
	 * @param effect			Optional additional effect(s)
	 * @param ammo				Optional ammo required for ranged weapons
	 */
	public Weapon(ObjectDescriptor descriptor, int durability, int speed, DamageEffect damage, Effect.Descriptor effect, String ammo) {
		super(descriptor, durability);
		Check.oneOrMore(speed);
		Check.notNull(damage);
		this.speed = speed;
		this.damage = damage;
		this.effect = Optional.ofNullable(effect);
		this.ammo = Optional.ofNullable(ammo);
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public DamageEffect getDamage() {
		return damage;
	}
	
	public Optional<Effect.Descriptor> getAdditionalEffects() {
		return effect;
	}
	
	public Optional<String> getAmmo() {
		return ammo;
	}
	
	@Override
	public DurableObject create() {
		return new DurableObject(this);
	}
}
