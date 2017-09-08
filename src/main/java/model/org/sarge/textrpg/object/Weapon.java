package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Optional;

import org.sarge.textrpg.entity.DamageEffect;
import org.sarge.textrpg.entity.Effect;

/**
 * Weapon.
 * @author Sarge
 */
public class Weapon extends DurableObject {
    /**
     * Weapon descriptor.
     */
    public static final class Descriptor extends DurableObject.Descriptor {
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
    	public Descriptor(ObjectDescriptor descriptor, int durability, int speed, DamageEffect damage, Effect.Descriptor effect, String ammo) {
    		super(descriptor, durability);
    		this.speed = oneOrMore(speed);
    		this.damage = notNull(damage);
    		this.effect = Optional.ofNullable(effect);
    		this.ammo = Optional.ofNullable(ammo);
    	}

    	/**
    	 * @return Weapon speed
    	 */
    	public int speed() {
    		return speed;
    	}

        /**
         * @return Damage-type
         */
    	public DamageEffect damage() {
    		return damage;
    	}

        /**
         * @return Effect(s) applied on a successful hit
         */
    	public Optional<Effect.Descriptor> effects() {
    		return effect;
    	}

        /**
         * @return Ammo for a ranged weapon
         */
    	public Optional<String> ammo() {
    		return ammo;
    	}

    	@Override
    	public DurableObject create() {
    		return new DurableObject(this);
    	}
    }

    /**
     * Constructor.
     * @param descriptor Weapon descriptor
     */
    public Weapon(Descriptor descriptor) {
        super(descriptor);
    }

    @Override
    public Descriptor descriptor() {
        return descriptor();
    }
}
