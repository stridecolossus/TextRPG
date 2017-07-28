package org.sarge.textrpg.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.object.DurableObject;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.MutableIntegerMap;

/**
 * Racial descriptor.
 * @author Sarge
 */
public final class Race {
	/**
	 * Racial attributes.
	 */
	public static final class Attributes {
		private boolean mount;
		private Gender gender = Gender.NEUTER;
		private Alignment align = Alignment.NEUTRAL;
		private Size size = Size.MEDIUM;
		private final MutableIntegerMap<Attribute> attrs = new MutableIntegerMap<Attribute>(Attribute.class);
		
		/**
		 * @return Whether this is a mount
		 */
		public boolean isMount() {
			return mount;
		}
		
		/**
		 * @return Default gender of this race
		 */
		public Gender getDefaultGender() {
			return gender;
		}
		
		/**
		 * @return Default alignment of this race
		 */
		public Alignment getDefaultAlignment() {
			return align;
		}
		
		/**
		 * @return Size of this race
		 */
		public Size getSize() {
			return size;
		}

		/**
		 * @return Default attributes for this race
		 */
		public IntegerMap<Attribute> getAttributes() {
			return attrs;
		}
		
		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}

	/**
	 * Default racial weapon that cannot be damaged.
	 */
	private static class DefaultWeapon extends DurableObject {
		public DefaultWeapon(Weapon weapon) {
			super(weapon);
		}
		
		@Override
		public void wear() throws ActionException {
			// Ignored
		}
		
		@Override
		protected void damage(DamageType type, int amount) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected void destroy() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Default gear and skills.
	 */
	public static final class RaceEquipment {
		/**
		 * Default weapon.
		 */
		private static final Weapon DEFAULT_WEAPON = new Weapon(new ObjectDescriptor("default.weapon"), 1, 1, new DamageEffect(DamageType.SLASHING, Value.ONE, false), null, null);

		private SkillSet skills = new SkillSet();
		private DurableObject weapon = new DefaultWeapon(DEFAULT_WEAPON);
		private Collection<ObjectDescriptor> equipment = new ArrayList<>();
		
		/**
		 * @return Default weapon for this race, e.g. fists
		 */
		public DurableObject getWeapon() {
			return weapon;
		}
		
		/**
		 * @return Default equipment for this race
		 */
		public Stream<ObjectDescriptor> getEquipment() {
			return equipment.stream();
		}
				
		/**
		 * @return Default skills for this race
		 */
		public SkillSet getSkills() {
			return new SkillSet(skills);
		}
		
		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}

	/**
	 * Kill descriptor.
	 */
	public static final class KillDescriptor {
		private boolean corporeal = true;
		private Optional<LootFactory> butcher = Optional.empty();
		// TODO
		//private final int xp;
		//private final List<Reward> rewards = new ArrayList<>();
		
		/**
		 * @return Whether this is a corporeal race
		 */
		public boolean isCorporeal() {
			return corporeal;
		}
		
		/**
		 * @return Loot-factory for butchering a corpse of this race
		 */
		public Optional<LootFactory> getButcherFactory() {
			return butcher;
		}
		
		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}
	
	private final String name;
	private final Attributes attrs;
	private final RaceEquipment equipment;
	private final KillDescriptor kill;
	
	/**
	 * Constructor.
	 * @param name			Name of this race
	 * @param attrs			Racial attributes
	 * @param equipment		Gear and skill
	 * @param kill			Kill descriptor
	 */
	public Race(String name, Attributes attrs, RaceEquipment equipment, KillDescriptor kill) {
		Check.notEmpty(name);
		Check.notNull(attrs);
		Check.notNull(equipment);
		Check.notNull(kill);
		this.name = name;
		this.attrs = attrs;
		this.equipment = equipment;
		this.kill = kill;
	}

	/**
	 * @return Racial name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return Whether this is a corporeal race
	 */
	public boolean isCorporeal() {
		return attrs.size != Size.NONE;
	}

	/**
	 * @return Racial attributes
	 */
	public Attributes getAttributes() {
		return attrs;
	}

	/**
	 * @return Default gear and skills
	 */
	public RaceEquipment getEquipment() {
		return equipment;
	}
	
	/**
	 * @return Kill descriptor
	 */
	public KillDescriptor getKillDescriptor() {
		return kill;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Builder for a {@link Race}.
	 */
	public static class Builder {
		private final String name;
		private Attributes attrs = new Attributes();
		private RaceEquipment equipment = new RaceEquipment();
		private KillDescriptor kill = new KillDescriptor();

		/**
		 * Constructor.
		 * @param name Race name
		 */
		public Builder(String name) {
			this.name = name;
			init();
		}
		
		/**
		 * Initialises this builder.
		 */
		private void init() {
			attrs = new Attributes();
			equipment = new RaceEquipment();
			kill = new KillDescriptor();
		}
		
		/**
		 * Sets this race as a mount.
		 */
		public Builder mount() {
			attrs.mount = true;
			return this;
		}

		/**
		 * Sets this race as a mount.
		 */
		public Builder notCorporeal() {
			kill.corporeal = false;
			return this;
		}

		/**
		 * Sets the gender of this race.
		 * @param gender Gender
		 */
		public Builder gender(Gender gender) {
			attrs.gender = gender;
			return this;
		}

		/**
		 * Sets the alignment of this race.
		 * @param align Alignment
		 */
		public Builder alignment(Alignment align) {
			attrs.align = align;
			return this;
		}

		/**
		 * Sets the size of this race.
		 * @param size Size
		 */
		public Builder size(Size size) {
			// TODO - is NONE valid?
			attrs.size = size;
			return this;
		}
		
		/**
		 * Sets an entity attribute.
		 * @param gender Gender
		 */
		public Builder attribute(Attribute attr, int value) {
			attrs.attrs.set(attr, value);
			return this;
		}

		/**
		 * @return Mutable attributes map
		 */
		public MutableIntegerMap<Attribute> getAttributes() {
			return attrs.attrs;
		}

		/**
		 * Sets the default weapon of this race.
		 * @param weapon Default weapon
		 */
		public Builder weapon(Weapon weapon) {
			equipment.weapon = new DefaultWeapon(weapon);
			return this;
		}

		/**
		 * Adds default equipment.
		 * @param descriptor Equipment descriptor
		 * @throws IllegalArgumentException if the given descriptor is not for equipment
		 */
		public Builder equipment(ObjectDescriptor descriptor) {
			equipment.equipment.add(descriptor);
			return this;
		}

		/**
		 * Adds a skill to this race.
		 * @param skill Skill
		 * @param level Level
		 */
		public Builder skills(SkillSet skills) {
			equipment.skills = skills;
			return this;
		}

		/**
		 * Sets the butchery loot factory for this race.
		 * @param butcher Butchery loot factory
		 */
		public Builder butcherFactory(LootFactory butcher) {
			kill.butcher = Optional.ofNullable(butcher);
			return this;
		}

		/**
		 * Constructs a new race.
		 * @return New race
		 */
		public Race build() {
			final Race race = new Race(name, attrs, equipment, kill);
			init();
			return race;
		}
	}
}
