package org.sarge.textrpg.object;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.StrictSet;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.util.Percentile;

/**
 * Descriptor for a class of objects.
 * @author Sarge
 * TODO
 * - how to have tiers of objects?  e.g. lock-picks: basic, standard, good, excellent, legendary -> improved durability, additional effects, etc
 * - but all have to be mapped to same descriptor?  or do we use categories (seems nasty?)
 */
public class ObjectDescriptor implements Cloneable {
	/**
	 * Default colour.
	 */
	public static final String COLOUR_NONE = "none";
	
	/**
	 * Default long description suffix.
	 */
	public static final String DEFAULT_DESCRIPTION = "dropped";
	
	/**
	 * Weight of an immovable object.
	 */
	public static final int IMMOVABLE = Integer.MAX_VALUE;

	/**
	 * Physical properties of this object.
	 */
	public static final class Properties {
		private int weight;
		private int value;
		private Size size = Size.NONE;
		private long reset;
		private long forget;
		
		/**
		 * @return Weight of this object or {@link ObjectDescriptor#IMMOVABLE} for a fixture
		 */
		public int getWeight() {
			return weight;
		}
		
		/**
		 * @return Shop value of this object
		 */
		public int getValue() {
			return value;
		}

		/**
		 * @return Size/bulkiness of this object
		 */
		public Size getSize() {
			return size;
		}

		/**
		 * @return Reset duration (ms)
		 */
		public long getResetPeriod() {
			return reset;
		}

		/**
		 * @return Forget duration (ms)
		 */
		public long getForgetPeriod() {
			return forget;
		}
	}

	/**
	 * Visible characteristics of this object.
	 */
	public static final class Characteristics {
		private String desc = DEFAULT_DESCRIPTION;
		private String col = COLOUR_NONE;
		private Cardinality cardinality = Cardinality.SINGLE;
		private Material mat = Material.DEFAULT;
		private final Set<String> cats = new StrictSet<>();
		private Percentile vis = Percentile.ONE;
		private final Map<Emission.Type, Emission> emissions = new StrictMap<>();
		private boolean quiet;
		
		/**
		 * @return Long description key suffix (default is {@link ObjectDescriptor#DEFAULT_DESCRIPTION})
		 */
		public String getFullDescriptionKey() {
			return desc;
		}
		
		/**
		 * @return Colour of this object or {@link ObjectDescriptor#COLOUR_NONE}
		 */
		public String getColour() {
			return col;
		}

		/**
		 * @return Object cardinality
		 */
		public Cardinality getCardinality() {
			return cardinality;
		}

		/**
		 * @return Material descriptor (default is {@link Material#DEFAULT})
		 */
		public Material getMaterial() {
			return mat;
		}
		
		/**
		 * @return None-or-more object categories
		 */
		public Stream<String> getCategories() {
			return cats.stream();
		}

		/**
		 * @return Default visibility of this object
		 */
		public Percentile getVisibility() {
			return vis;
		}

		/**
		 * @param type Type of emission
		 * @return Emission descriptor
		 */
		public Optional<Emission> getEmission(Emission.Type type) {
			return Optional.ofNullable(emissions.get(type));
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
	public static final class Equipment {
		private Optional<DeploymentSlot> slot = Optional.empty();
		private boolean two;
		private Condition condition = Condition.TRUE;
		private int armour;
		private Effect.Descriptor passive = Effect.NONE;

		/**
		 * @return Deployment slot for an object that can be equipped
		 */
		public Optional<DeploymentSlot> getDeploymentSlot() {
			return slot;
		}

		/**
		 * @return Whether this object requires both hands to equip
		 */
		public boolean isTwoHanded() {
			return two;
		}

		/**
		 * @return Condition(s) required to equip this object
		 */
		public Condition getCondition() {
			return condition;
		}

		/**
		 * @return Armour rating
		 */
		public int getArmour() {
			return armour;
		}

		/**
		 * @return Passive effect(s)
		 */
		public Effect.Descriptor getPassive() {
			return passive;
		}
	}
	
	private final String name;
	private final Properties props;
	private final Characteristics chars;
	private final Equipment equipment;
	
	/**
	 * Constructor.
	 * @param name			Object name
	 * @param props			Physical properties of this object
	 * @param chars			Visible characteristics of this object
	 * @param equipment		Optional descriptor for an equipped object
	 * @param reset			Reset period (ms)
	 */
	public ObjectDescriptor(String name, Properties props, Characteristics chars, Equipment equipment) {
		Check.notEmpty(name);
		Check.notNull(props);
		Check.notNull(chars);
		this.name = name;
		this.props = props;
		this.chars = chars;
		this.equipment = equipment;
	}
	
	/**
	 * Convenience constructor for a simple descriptor.
	 * @param name Object name
	 */
	public ObjectDescriptor(String name) {
		this(name, new Properties(), new Characteristics(), null);
	}

	/**
	 * Copy constructor for sub-classes.
	 * @param descriptor Underlying descriptor
	 */
	protected ObjectDescriptor(ObjectDescriptor descriptor) {
		this(descriptor.name, descriptor.props, descriptor.chars, descriptor.equipment);
	}
	
	/**
	 * @return Object name
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * @return Description key for objects of this type (default is <b>object</b>)
	 */
	public String getDescriptionKey() {
		return "object";
	}
	
	/**
	 * @return Physical properties of this object
	 */
	public final Properties getProperties() {
		return props;
	}

	/**
	 * @return Visible characteristics of this object
	 */
	public final Characteristics getCharacteristics() {
		return chars;
	}

	/**
	 * @return Equipment descriptor if this object can be equipped
	 */
	public final Equipment getEquipment() {
		return equipment;
	}

	/**
	 * @return Whether this descriptor <b>cannot</b> be used as a fixture
	 * @see Properties#getWeight()
	 * @see WorldObject#isFixture()
	 */
	public boolean isTransient() {
		return props.weight < IMMOVABLE;
	}

	/**
	 * Helper.
	 * @param cat Category
	 * @return Whether this descriptor has the given category
	 */
	public final boolean isCategory(String cat) {
		return chars.cats.contains(cat);
	}

	/**
	 * Factory for new objects defined by this descriptor.
	 * @return New object
	 */
	public WorldObject create() {
		return new WorldObject(this);
	}

	/**
	 * Converts this descriptor to a fixture.
	 * @return Fixture descriptor
	 * @throws IllegalArgumentException if this descriptor is already a fixture
	 * @see #isTransient()
	 */
	public final ObjectDescriptor toFixture() {
		// Ignore if already a fixture
		if(!isTransient()) return this;
		
		// Otherwise clone a fixture descriptor
		final ObjectDescriptor descriptor;
		try {
			descriptor = (ObjectDescriptor) this.clone();
		}
		catch(CloneNotSupportedException e) {
			throw new RuntimeException();
		}
		descriptor.props.weight = IMMOVABLE;
		return descriptor;
	}

	/**
	 * Verifies this is a descriptor for a resetable object.
	 */
	protected final void verifyResetable() {
		if(props.reset == 0) throw new IllegalArgumentException("Expected reset/forget periods");
	}
	
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Builder for an object descriptor.
	 */
	public static class Builder {
		private final String name;
		private final Properties props = new Properties();
		private final Characteristics chars = new Characteristics();
		private Equipment equipment = new Equipment();
		
		/**
		 * Constructor.
		 * @param name Object name
		 */
		public Builder(String name) {
			Check.notEmpty(name);
			this.name = name;
		}

		/**
		 * Sets the weight of this object.
		 * @param weight Object weight
		 */
		public Builder weight(int weight) {
			Check.zeroOrMore(weight);
			props.weight = weight;
			return this;
		}

		/**
		 * Sets the size of this object.
		 * @param size Object size
		 */
		public Builder size(Size size) {
			Check.notNull(size);
			props.size = size;
			return this;
		}

		/**
		 * Sets the value of this object.
		 * @param value Object value
		 */
		public Builder value(int value) {
			Check.zeroOrMore(value);
			props.value = value;
			return this;
		}

		/**
		 * Sets the reset period of this object.
		 * @param reset Reset period (ms)
		 */
		public Builder reset(long reset) {
			Check.zeroOrMore(reset);
			this.props.reset = reset;
			return this;
		}

		/**
		 * Sets the forget period of this object.
		 * @param forget Forget period (ms)
		 */
		public Builder forget(long forget) {
			Check.zeroOrMore(forget);
			this.props.forget = forget;
			return this;
		}

		/**
		 * Sets the long description key suffix for this object.
		 * @param desc Long description suffix
		 */
		public Builder description(String desc) {
			Check.notEmpty(desc);
			chars.desc = desc;
			return this;
		}

		/**
		 * Sets the colour of this object.
		 * @param col Object colour
		 * @see ObjectDescriptor#COLOUR_NONE
		 */
		public Builder colour(String col) {
			Check.notEmpty(col);
			chars.col = col;
			return this;
		}

		/**
		 * Sets the cardinality of this object.
		 * @param cardinality Cardinality
		 */
		public Builder cardinality(Cardinality cardinality) {
			Check.notNull(cardinality);
			chars.cardinality = cardinality;
			return this;
		}

		/**
		 * Sets the material of this object.
		 * @param mat Material descriptor
		 * @see Material#DEFAULT
		 */
		public Builder material(Material mat) {
			Check.notNull(mat);
			chars.mat = mat;
			return this;
		}
		
		/**
		 * Adds a category for this object.
		 * @param cat Category
		 */
		public Builder category(String cat) {
			Check.notEmpty(cat);
			chars.cats.add(cat);
			return this;
		}
		
		/**
		 * Sets the default visibility of this object.
		 * @param vis Visibility
		 */
		public Builder visibility(Percentile vis) {
			Check.notNull(vis);
			chars.vis = vis;
			return this;
		}

		/**
		 * Adds an emission emanating from this object.
		 * @param emission Emission descriptor
		 */
		public Builder emission(Emission emission) {
			chars.emissions.put(emission.getType(), emission);
			return this;
		}
		
		/**
		 * Makes this a silent object.
		 */
		public Builder quiet() {
			chars.quiet = true;
			return this;
		}

		/**
		 * Makes this object equipped.
		 * @param slot Deployment slot
		 */
		public Builder slot(DeploymentSlot slot) {
			equipment.slot = Optional.ofNullable(slot);
			return this;
		}

		/**
		 * Sets whether this equipment is two-handed.
		 * @param two Whether two-handed
		 */
		public Builder twoHanded(boolean two) {
			equipment.two = two;
			return this;
		}
		
		/**
		 * Sets the condition(s) for wearing this equipment.
		 * @param condition Condition(s)
		 */
		public Builder condition(Condition condition) {
			Check.notNull(condition);
			equipment.condition = condition;
			return this;
		}
		
		/**
		 * Sets the armour rating of this equipment.
		 * @param armour Armour rating
		 */
		public Builder armour(int armour) {
			Check.zeroOrMore(armour);
			equipment.armour = armour;
			return this;
		}
		
		/**
		 * Sets the passive effect(s) of this equipment.
		 * @param passive Passive effect(s)
		 */
		public Builder passive(Effect.Descriptor passive) {
			Check.notNull(passive);
			equipment.passive = passive;
			return this;
		}

		/**
		 * Constructs a new descriptor.
		 * @return Object descriptor
		 */
		public ObjectDescriptor build() {
			return new ObjectDescriptor(name, props, chars, equipment);
		}
	}
}
