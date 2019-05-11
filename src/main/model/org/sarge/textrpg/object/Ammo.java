package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Randomiser;

/**
 * Ammo for ranged weapons.
 * @see Weapon.Descriptor#ammo()
 */
public class Ammo extends ObjectStack {
	/**
	 * Ammo types.
	 */
	public enum Type {
		ARROW,
		BOLT,
		STONE;

		/**
		 * @return Whether this type of ammo can be recovered
		 */
		public boolean isRecoverable() {
			return this != STONE;
		}
	}

	/**
	 * Ammo descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Type type;
		private final Damage damage;
		private final Percentile durability;

		/**
		 * Constructor.
		 * @param descriptor		Descriptor
		 * @param type				Type of ammo
		 * @param damage			Ranged damage effect
		 * @param durability		Chance that ammo survives being used
		 */
		public Descriptor(ObjectDescriptor descriptor, Type type, Damage damage, Percentile durability) {
			super(descriptor);
			this.type = notNull(type);
			this.damage = notNull(damage);
			this.durability = notNull(durability);
		}

		@Override
		public boolean isStackable() {
			return true;
		}

		/**
		 * @return Type of this ammo
		 */
		public Type type() {
			return type;
		}

		/**
		 * @return Ranged damage effect
		 */
		public Damage damage() {
			return damage;
		}

		/**
		 * @return Chance that ammo survives being used
		 */
		public Percentile durability() {
			return durability;
		}

		@Override
		public Ammo create() {
			return new Ammo(this, 1);
		}
	}

	/**
	 * Constructor.
	 * @param descriptor Ammo descriptor
	 */
	public Ammo(Descriptor descriptor, int count) {
		super(descriptor, count);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	@Override
	public boolean isCategory(String cat) {
		if(this.descriptor().type.name().equals(cat)) {
			return true;
		}
		else {
			return super.isCategory(cat);
		}
	}

	/**
	 *
	 * TODO - move to shoot action
	 *
	 * Consumes this ammo (either destroys or hides in the current location).
	 * @param actor		Actor
	 * @param vis		Visibility of surviving ammo
	 * @see HiddenObject
	 * @throws IllegalStateException if this ammo is a stack
	 */
	public void consume(Actor actor, Percentile vis) {
		if(count() != 1) throw new IllegalStateException("Cannot consume an ammo stack");

		// Determine whether survives
		final Descriptor descriptor = this.descriptor();
		final boolean survives = descriptor.type.isRecoverable() && Randomiser.isLessThan(descriptor.durability);

		// Consume
		if(survives) {
			HiddenObject.hide(this, vis, actor);
		}
		else {
			destroy();
		}
	}
}
