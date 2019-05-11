package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.ValueModifier;

/**
 * Descriptor for a <i>class</i> of objects.
 * @author Sarge
 */
public class ObjectDescriptor extends AbstractEqualsObject {
	/**
	 * Weight of a fixture.
	 */
	public static final int FIXTURE = Integer.MAX_VALUE;

	/**
	 * Category for a consumable object, e.g. bandages.
	 */
	public static final String CONSUMABLE = "consumable";

	/**
	 * Descriptor filter.
	 */
	public interface Filter extends Predicate<ObjectDescriptor> {
		/**
		 * Filter that matches <b>all</b> descriptors.
		 */
		Filter ALL = desc -> true;

		/**
		 * Filter for one-handed objects.
		 */
		Filter ONE_HANDED = handed(false);

		/**
		 * Filter for two-handed objects.
		 */
		Filter TWO_HANDED = handed(true);

		/**
		 * Creates a filter that matches the descriptor.
		 * @param descriptor Object descriptor
		 * @return Filter
		 */
		static Filter of(ObjectDescriptor descriptor) {
			return desc -> desc == descriptor;
		}

		/**
		 * Creates a deployment-slot filter.
		 * @param slot Deployment-slot
		 * @return Deployment-slot filter
		 */
		static Filter slot(Slot slot) {
			return desc -> desc.equipment.slot == slot;
		}

		/**
		 * Creates a one/two-handed filter.
		 * @param two Whether one or two-handed
		 * @return Handed filter
		 */
		private static Filter handed(boolean two) {
			return desc -> desc.props.isTwoHanded() == two;
		}
	}

	/**
	 * Descriptor for a passive effect on an piece of equipment.
	 */
	public static final class PassiveEffect extends AbstractEqualsObject {
		private final ValueModifier.Key mod;
		private final int size;

		/**
		 * Constructor.
		 * @param mod			Modifier
		 * @param size			Magnitude
		 */
		public PassiveEffect(ValueModifier.Key mod, int size) {
			this.mod = notNull(mod);
			this.size = oneOrMore(size);
		}

		/**
		 * @return Modifier
		 */
		public ValueModifier.Key modifier() {
			return mod;
		}

		/**
		 * @return Effect magnitude
		 */
		public int size() {
			return size;
		}
	}

	/**
	 * Physical properties of this object.
	 */
	public static final class Properties extends AbstractEqualsObject {
		private final int weight;
		private final int value;
		private final Size size;
		private final Alignment alignment;
		private final boolean two;
		private final Duration reset;
		private final Duration decay;

		/**
		 * Constructor.
		 * @param weight		Weight of this object
		 * @param value			Buy/sell value of this object
		 * @param size			Size of this object
		 * @param alignment		Alignment of this object
		 * @param two			Whether this object requires both hands when carried or equipped, e.g. a bow or a large basket of apples
		 * @param reset			Reset period if this object can be reset
		 * @param decay			Decay period when this object has been dropped
		 */
		private Properties(int weight, int value, Size size, Alignment alignment, boolean two, Duration reset, Duration decay) {
            this.weight = zeroOrMore(weight);
            this.value = zeroOrMore(value);
            this.size = notNull(size);
            this.alignment = notNull(alignment);
            this.two = two;
            this.reset = notNull(reset);
            this.decay = notNull(decay);
        }

        /**
		 * @return Weight of this object or {@link ObjectDescriptor#FIXTURE} for a fixture
		 */
		public int weight() {
			return weight;
		}

		/**
		 * @return Trade value of this object
		 */
		public int value() {
			return value;
		}

		/**
		 * @return Size/bulkiness of this object
		 */
		public Size size() {
			return size;
		}

		/**
		 * @return Alignment of this object
		 */
		public Alignment alignment() {
			return alignment;
		}

		/**
		 * @return Whether this is a two-handed object
		 */
		public boolean isTwoHanded() {
			return two;
		}

		/**
		 * @return Reset duration
		 */
		public Duration reset() {
			return reset;
		}

		/**
		 * @return Decay period
		 */
		public Duration decay() {
			return decay;
		}
	}

	/**
	 * Visible characteristics of this object.
	 */
	public static final class Characteristics extends AbstractEqualsObject {
		/**
		 * Default placement key.
		 */
		public static final String PLACEMENT_DEFAULT = "default";

		private final String placement;
		private final Optional<String> qualifier;
		private final Cardinality cardinality;
		private final Material mat;
		private final Set<String> cats;
		private final Percentile vis;
		private final boolean quiet;

		/**
		 * Constructor.
		 * @param placement			Placement description key
		 * @param qualifier			Optional object qualifier
		 * @param cardinality		Cardinality
		 * @param mat				Material descriptor
		 * @param cats				Categories
		 * @param vis				Visibility
		 * @param quiet				Whether this object is omitted from location descriptions
		 */
        private Characteristics(String placement, String qualifier, Cardinality cardinality, Material mat, Set<String> cats, Percentile vis, boolean quiet) {
            this.placement = notEmpty(placement);
            this.qualifier = Optional.ofNullable(qualifier);
            this.cardinality = notNull(cardinality);
            this.mat = notNull(mat);
            this.cats = new StrictSet<>(cats);
            this.vis = notNull(vis);
            this.quiet = quiet;
        }

        /**
		 * @return Placement key (default is {@link #PLACEMENT_DEFAULT})
		 */
		public String placement() {
			return placement;
		}

		/**
		 * @return Object qualifier
		 */
		public Optional<String> qualifier() {
			return qualifier;
		}

		/**
		 * @return Object cardinality
		 */
		public Cardinality cardinality() {
			return cardinality;
		}

		/**
		 * @return Material descriptor (default is {@link Material#NONE})
		 */
		public Material material() {
			return mat;
		}

		/**
		 * @return None-or-more object categories
		 */
		public Set<String> categories() {
			return cats;
		}

		/**
		 * @return Default visibility of this object
		 */
		public Percentile visibility() {
			return vis;
		}

		/**
		 * @return Whether this object is omitted from location descriptions
		 */
		public boolean isQuiet() {
			return quiet;
		}
	}

	/**
	 * Descriptor for an equipped object.
	 */
	public static final class EquipmentDescriptor extends AbstractEqualsObject {
		private static final EquipmentDescriptor NONE = new EquipmentDescriptor();

		private final Slot slot;
		private final List<Condition> conditions;
		private final int armour;
		private final int warmth;
		private final Percentile noise;
		private final List<PassiveEffect> passive;

		/**
		 * Constructor.
		 * @param slot			Deployment slot
		 * @param condition		Condition(s) required to equip this object
		 * @param armour		Armour rating
		 * @param warmth		Warmth rating
		 * @param noise			Noise generated by this equipment
		 * @param passive		Passive effect(s)
		 */
		private EquipmentDescriptor(Slot slot, List<Condition> conditions, int armour, int warmth, Percentile noise, List<PassiveEffect> passive) {
			this.slot = notNull(slot);
			this.conditions = List.copyOf(conditions);
			this.armour = zeroOrMore(armour);
			this.warmth = zeroOrMore(warmth);
			this.noise = notNull(noise);
			this.passive = List.copyOf(passive);
		}

		/**
		 * Constructor for an object that cannot be equipped.
		 */
		private EquipmentDescriptor() {
			slot = Slot.NONE;
			conditions = List.of();
			armour = 0;
			warmth = 0;
			noise = Percentile.ZERO;
			passive = List.of();
		}

		/**
		 * @return Deployment slot for an object that can be equipped
		 */
		public Slot slot() {
			return slot;
		}

		/**
		 * @return Condition(s) required to equip this object
		 */
		public List<Condition> conditions() {
			return conditions;
		}

		/**
		 * @return Armour rating
		 */
		public int armour() {
			return armour;
		}

		/**
		 * @return Warmth rating
		 */
		public int warmth() {
			return warmth;
		}

		/**
		 * @return Noise generated by this equipment
		 */
		public Percentile noise() {
			return noise;
		}

		/**
		 * @return Passive effect(s)
		 */
		public List<PassiveEffect> passive() {
			return passive;
		}
	}

	/**
	 * Creates a basic object descriptor with the given name.
	 * @param name Name
	 * @return Basic object descriptor
	 */
	public static ObjectDescriptor of(String name) {
		return new ObjectDescriptor.Builder(name).build();
	}

	/**
	 * Creates a fixture.
	 * @param name Name
	 * @return Object descriptor for a fixture
	 * @see #FIXTURE
	 */
	public static ObjectDescriptor fixture(String name) {
		return new ObjectDescriptor.Builder(name).fixture().build();
	}

	private final String name;
	private final Properties props;
	private final Characteristics chars;
	private final EquipmentDescriptor equipment;

	/**
	 * Constructor.
	 * @param name			Object name
	 * @param props			Physical properties of this object
	 * @param chars			Visible characteristics of this object
	 * @param equipment		Optional descriptor for an equipped object
	 */
	private ObjectDescriptor(String name, Properties props, Characteristics chars, EquipmentDescriptor equipment) {
		this.name = notEmpty(name);
		this.props = notNull(props);
		this.chars = notNull(chars);
		this.equipment = notNull(equipment);
		verify();
	}

	/**
	 * Copy constructor for sub-classes.
	 * @param descriptor Underlying descriptor
	 */
	protected ObjectDescriptor(ObjectDescriptor descriptor) {
		this(descriptor.name, descriptor);
	}

	/**
	 * Copy constructor that over-rides the name of this object.
	 * @param name			Name
	 * @param descriptor	Underlying descriptor
	 */
	protected ObjectDescriptor(String name, ObjectDescriptor descriptor) {
		this(name, descriptor.props, descriptor.chars, descriptor.equipment);
	}

	/**
	 * Verifies this descriptor.
	 */
	private void verify() {
		// Check resetable objects
		if(isResetable() && (props.reset == Duration.ZERO)) {
			throw new IllegalArgumentException("Expected reset period: " + name);
		}

		// Check two-handed equipment
		if(props.two && (equipment.slot != Slot.MAIN)) {
			throw new IllegalArgumentException("Two-handed equipment can only be deployed to MAIN_HAND");
		}

		// Check stacked objects
		if(isStackable()) {
			if(isFixture()) throw new IllegalArgumentException("Cannot stack fixtures");
			if(isResetable()) throw new IllegalArgumentException("Cannot stack resetable objects");
		}
	}

	/**
	 * @return Object name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Physical properties of this object
	 */
	public final Properties properties() {
		return props;
	}

	/**
	 * @return Visible characteristics of this object
	 */
	public final Characteristics characteristics() {
		return chars;
	}

	/**
	 * @return Equipment descriptor
	 */
	public final EquipmentDescriptor equipment() {
		return equipment;
	}

	/**
	 * @return Whether this descriptor is a fixture
	 */
	public boolean isFixture() {
		return props.weight == FIXTURE;
	}

	/**
	 * @return Whether this descriptor requires a reset period to be defined (default is <tt>false</tt>)
	 * @see #verify()
	 */
	public boolean isResetable() {
		return false;
	}

	/**
	 * @return Whether this object is perishable, i.e. food
	 */
	public boolean isPerishable() {
		return false;
	}

	/**
	 * @return Whether objects defined by this descriptor can be stacked (default is <tt>false</tt>)
	 * @see ObjectStack
	 */
	public boolean isStackable() {
		return false;
	}

	/**
	 * Factory for new objects defined by this descriptor.
	 * @return New object
	 * @see WorldObject#WorldObject(ObjectDescriptor)
	 */
	public WorldObject create() {
		return new WorldObject(this);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Builder for an object descriptor.
	 */
	public static class Builder {
		// Properties
		private final String name;
        private int weight;
        private int value;
        private Size size = Size.NONE;
        private Alignment alignment = Alignment.NEUTRAL;
		private boolean two;
        private Duration reset = Duration.ZERO;
        private Duration decay = Duration.ZERO;

        // Characteristics
        private String placement = Characteristics.PLACEMENT_DEFAULT;
        private String qualifier;
        private Cardinality cardinality = Cardinality.SINGLE;
        private Material mat = Material.NONE;
        private final Set<String> cats = new StrictSet<>();
        private Percentile vis = Percentile.ONE;
        private boolean quiet;

        // Equipment
		private Slot slot = Slot.NONE;
		private List<Condition> conditions = new StrictList<>();
		private int armour;
		private int warmth;
		private Percentile noise = Percentile.ZERO;
		private List<PassiveEffect> passive = new StrictList<>();

		/**
		 * Constructor.
		 * @param name Object name
		 */
		public Builder(String name) {
			this.name = name;
		}

        /**
         * Sets the weight of this object.
         * @param weight Object weight
         */
        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        /**
         * Sets this descriptor as a fixture.
         */
        public Builder fixture() {
        	weight = FIXTURE;
        	return this;
        }

        /**
         * Sets the size of this object.
         * @param size Object size
         */
        public Builder size(Size size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the alignment of this object.
         * @param alignment Alignment
         */
        public Builder alignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

		/**
		 * Sets this object as two-handed.
		 */
		public Builder twoHanded() {
			this.two = true;
			this.slot = Slot.MAIN;
			return this;
		}

        /**
         * Sets the value of this object.
         * @param value Object value
         */
        public Builder value(int value) {
            this.value = value;
            return this;
        }

        /**
         * Sets the reset period of this object.
         * @param reset Reset period (ms)
         */
        public Builder reset(Duration reset) {
            this.reset = reset;
            return this;
        }

        /**
         * Sets the decay period of this object.
         * @param decay Decay period (ms)
         */
        public Builder decay(Duration decay) {
            this.decay = decay;
            return this;
        }

        /**
         * Sets the placement description key for this object.
         * @param placement Placement key
         */
        public Builder placement(String placement) {
            this.placement = placement;
            return this;
        }

        /**
         * Sets the qualifier of this object.
         * @param qualifier Qualifier
         */
        public Builder qualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        /**
         * Sets the cardinality of this object.
         * @param cardinality Cardinality
         */
        public Builder cardinality(Cardinality cardinality) {
            this.cardinality = cardinality;
            return this;
        }

        /**
         * Sets the material of this object.
         * @param mat Material descriptor
         * @see Material#DEFAULT_PROPERTIES
         */
        public Builder material(Material mat) {
            this.mat = mat;
            return this;
        }

        /**
         * Adds a category for this object.
         * @param cat Category
         */
        public Builder category(String cat) {
            this.cats.add(cat);
            return this;
        }

        /**
         * Sets the default visibility of this object.
         * @param vis Visibility
         * TODO - should we enforce forget-period here?
         */
        public Builder visibility(Percentile vis) {
            this.vis = vis;
            return this;
        }

        /**
         * Makes this a silent object.
         */
        public Builder quiet(boolean quiet) {
            this.quiet = quiet;
            return this;
        }

		/**
		 * Makes this object equipped.
		 * @param slot Deployment slot
		 */
		public Builder slot(Slot slot) {
			this.slot = slot;
			return this;
		}

		/**
		 * Adds a condition for wearing this equipment.
		 * @param c Condition
		 */
		public Builder condition(Condition c) {
			conditions.add(c);
			return this;
		}

		/**
		 * Sets the armour rating of this equipment.
		 * @param armour Armour rating
		 */
		public Builder armour(int armour) {
			this.armour = armour;
			return this;
		}

		/**
		 * Sets the warmth rating of this equipment.
		 * @param warmth Warmth rating
		 */
		public Builder warmth(int warmth) {
			this.warmth = warmth;
			return this;
		}

		/**
		 * Sets the intensity of noise generated by this equipment.
		 * @param noise Noise intensity
		 */
		public Builder noise(Percentile noise) {
			this.noise = noise;
			return this;
		}

		/**
		 * Adds a passive effect for this equipment.
		 * @param passive Passive effect
		 */
		public Builder passive(PassiveEffect p) {
			passive.add(p);
			return this;
		}

		/**
		 * @return New object descriptor
		 */
		public ObjectDescriptor build() {
			final Properties props = new Properties(weight, value, size, alignment, two, reset, decay);
			final Characteristics chars = new Characteristics(placement, qualifier, cardinality, mat, cats, vis, quiet);
			EquipmentDescriptor equipment = new EquipmentDescriptor(slot, conditions, armour, warmth, noise, passive);
			if(equipment.equals(EquipmentDescriptor.NONE)) {
				equipment = EquipmentDescriptor.NONE;
			}
			else {
				if(slot == Slot.NONE) throw new IllegalArgumentException("Equipment properties specified but no deployment-slot");
			}
			return new ObjectDescriptor(name, props, chars, equipment);
		}
	}
}
