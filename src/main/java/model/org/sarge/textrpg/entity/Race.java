package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.object.ToString;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Value;
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
		private final MutableIntegerMap<Attribute> attrs = new MutableIntegerMap<>(Attribute.class);

		/**
		 * @return Whether this is a mount
		 */
		public boolean isMount() {
			return mount;
		}

		/**
		 * @return Default gender of this race
		 */
		public Gender gender() {
			return gender;
		}

		/**
		 * @return Default alignment of this race
		 */
		public Alignment alignment() {
			return align;
		}

		/**
		 * @return Size of this race
		 */
		public Size size() {
			return size;
		}

		/**
		 * @return Default attributes for this race
		 */
		public IntegerMap<Attribute> attributes() {
			return attrs;
		}

		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}

	/**
	 * Default gear and skills.
	 */
	public static final class RaceEquipment {
		/**
		 * Default weapon.
		 */
		private static final Weapon.Descriptor DEFAULT_WEAPON = new Weapon.Descriptor(new ObjectDescriptor("default.weapon"), 1, 1, new DamageEffect(DamageType.SLASHING, Value.ONE, false), null, null);

		private SkillSet skills = new SkillSet();
		private Weapon.Descriptor weapon = DEFAULT_WEAPON;
		private Collection<ObjectDescriptor> equipment = new ArrayList<>();

		/**
		 * @return Default weapon descriptor for this race, e.g. fists
		 */
		public Weapon.Descriptor weapon() {
			return weapon;
		}

		/**
		 * @return Default equipment for this race
		 */
		public Stream<ObjectDescriptor> equipment() {
			return equipment.stream();
		}

		/**
		 * @return Default skills for this race
		 */
		public SkillSet skills() {
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
		public Optional<LootFactory> butcherFactory() {
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
		this.name = notEmpty(name);
		this.attrs = notNull(attrs);
		this.equipment = notNull(equipment);
		this.kill = notNull(kill);
	}

	/**
	 * @return Racial name
	 */
	public String name() {
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
	public Attributes attributes() {
		return attrs;
	}

	/**
	 * @return Default gear and skills
	 */
	public RaceEquipment equipment() {
		return equipment;
	}

	/**
	 * @return Kill descriptor
	 */
	public KillDescriptor killDescriptor() {
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
		private Attributes attrs;
		private RaceEquipment equipment;
		private KillDescriptor kill;

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
			attrs.gender = notNull(gender);
			return this;
		}

		/**
		 * Sets the alignment of this race.
		 * @param align Alignment
		 */
		public Builder alignment(Alignment align) {
			attrs.align = notNull(align);
			return this;
		}

		/**
		 * Sets the size of this race.
		 * @param size Size
		 */
		public Builder size(Size size) {
			// TODO - is NONE valid?
			attrs.size = notNull(size);
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
		 * TODO - this is nasty (should not expose whole mutable map)
		 */
		public MutableIntegerMap<Attribute> getAttributes() {
			return attrs.attrs;
		}

		/**
		 * Sets the default weapon of this race.
		 * @param weapon Default weapon
		 */
		public Builder weapon(Weapon.Descriptor weapon) {
			equipment.weapon = notNull(weapon);
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
			equipment.skills = notNull(skills);
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
