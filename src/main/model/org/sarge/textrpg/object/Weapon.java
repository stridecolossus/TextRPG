package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Optional;

import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;

/**
 * Weapon.
 * @author Sarge
 */
public class Weapon extends DurableObject {
	/**
	 * Weapon descriptor.
	 */
	public static class Descriptor extends DurableObject.Descriptor {
		private final Skill skill;
		private final int speed;
		private final Damage damage;
		private final Optional<Ammo.Type> ammo;
		private final Optional<String> enemy;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param skill				Skill required to wield this weapon
		 * @param speed				Weapon speed
		 * @param damage			Damage descriptor
		 * @param ammo				Optional ammo for a ranged weapon
		 * @param enemy				Optional enemy type that causes this weapon to glow
		 */
		public Descriptor(DurableObject.Descriptor descriptor, Skill skill, int speed, Damage damage, Ammo.Type ammo, String enemy) {
			super(descriptor);
			this.skill = notNull(skill);
			this.speed = oneOrMore(speed);
			this.damage = notNull(damage);
			this.ammo = Optional.ofNullable(ammo);
			this.enemy = Optional.ofNullable(enemy);
			if(descriptor.equipment().slot() != Slot.MAIN) throw new IllegalArgumentException("Weapon must be deployed to main-hand");
		}

		/**
		 * @return Skill required to wield this weapon
		 */
		public Skill skill() {
			return skill;
		}

		/**
		 * @return Attack speed of this weapon
		 */
		public int speed() {
			return speed;
		}

		/**
		 * @return Weapon damage
		 */
		public Damage damage() {
			return damage;
		}

		/**
		 * @return Ammo type if this is a ranged weapon
		 */
		public Optional<Ammo.Type> ammo() {
			return ammo;
		}

		/**
		 * @return Enemy type that causes this weapon to glow
		 */
		public Optional<String> enemy() {
			return enemy;
		}

		@Override
		public Weapon create() {
			if(isCrossbow()) {
				return new Crossbow(this);
			}
			else {
				return new Weapon(this);
			}
		}

		/**
		 * @return Whether this weapon is a crossbow
		 */
		private boolean isCrossbow() {
			return ammo.map(type -> type == Ammo.Type.BOLT).orElse(false);
		}

		/**
		 * Convenience builder for a weapon descriptor.
		 */
		public static class Builder {
			private DurableObject.Descriptor descriptor = new DurableObject.Descriptor(new ObjectDescriptor.Builder("weapon").slot(Slot.MAIN).build(), 1);
			private Skill skill = Skill.NONE;
			private int speed = 1;
			private Damage damage = Damage.DEFAULT;
			private Ammo.Type ammo;
			private String enemy;

			/**
			 * Sets the durable descriptor of this weapon.
			 * @param descriptor Durable descriptor
			 */
			public Builder descriptor(DurableObject.Descriptor descriptor) {
				this.descriptor = descriptor;
				return this;
			}

			/**
			 * Sets the required skill for this weapon.
			 * @param skill Required skill
			 */
			public Builder skill(Skill skill) {
				this.skill = skill;
				return this;
			}

			/**
			 * Sets the speed of this weapon.
			 * @param speed Weapon speed
			 */
			public Builder speed(int speed) {
				this.speed = speed;
				return this;
			}

			/**
			 * Sets the damage descriptor for this weapon.
			 * @param damage Damage
			 */
			public Builder damage(Damage damage) {
				this.damage = damage;
				return this;
			}

			/**
			 * Sets the ammo type for this ranged weapon.
			 * @param ammo Ammo type
			 */
			public Builder ammo(Ammo.Type ammo) {
				this.ammo = ammo;
				return this;
			}

			/**
			 * Sets the enemy for this glowing weapon.
			 * @param enemy Enemy category
			 */
			public Builder enemy(String enemy) {
				this.enemy = enemy;
				return this;
			}

			/**
			 * constructs this weapon descriptor.
			 * @return New weapon descriptor
			 */
			public Weapon.Descriptor build() {
				return new Weapon.Descriptor(descriptor, skill, speed, damage, ammo, enemy);
			}
		}
	}

	/**
	 * Crossbow that can be pre-loaded with a {@link Ammo.Format.BOLT}.
	 */
	static class Crossbow extends Weapon {
		private Ammo loaded;

		/**
		 * Constructor.
		 * @param descriptor Weapon descriptor
		 */
		private Crossbow(Descriptor descriptor) {
			super(descriptor);
		}

		@Override
		protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
			if(carried && isLoaded()) {
				builder.add("loaded", loaded.name());
			}
			super.describe(carried, builder, formatters);
		}

		/**
		 * @return Whether this crossbow has a bolt loaded
		 */
		public boolean isLoaded() {
			if(loaded == null) {
				return false;
			}
			else {
				if(loaded.parent() != this.parent()) {
					// TODO - does this work?
					loaded = null;
					return false;
				}
				else {
					return true;
				}
			}
		}

		/**
		 * Loads a bolt into a crossbow.
		 * @param bolt Crossbow bolt
		 * @throws ActionException if this crossbow is already loaded
		 * @throws IllegalArgumentException if the given ammo is not a crossbow bolt
		 */
		public void load(Ammo ammo) throws ActionException {
			if(ammo.descriptor().type() != Ammo.Type.BOLT) throw new IllegalArgumentException("Not a crossbow bolt: " + ammo);
			if(isLoaded()) throw ActionException.of("crossbow.already.loaded");
			loaded = ammo;
		}

		/**
		 * Unloads this crossbow.
		 * @throws ActionException if this crossbow is not loaded
		 */
		public void unload() throws ActionException {
			if(!isLoaded()) throw ActionException.of("crossbow.not.loaded");
			loaded = null;
		}
	}

	/**
	 * Constructor.
	 * @param descriptor Weapon descriptor
	 */
	protected Weapon(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}
}
