package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.Converter;
import org.sarge.textrpg.util.ValueModifier;

/**
 * Transient entity values.
 * @author Sarge
 */
public enum EntityValue {
	// Primary values
	HEALTH(Type.PRIMARY),
	STAMINA(Type.PRIMARY),
	POWER(Type.PRIMARY),

	// Cumulative
	THIRST(Type.PERCENTILE),
	HUNGER(Type.PERCENTILE),

	// Combat
	BLOCK(Type.DEFAULT),
	PARRY(Type.DEFAULT),
	DODGE(Type.DEFAULT),

	// Effects
	VISIBILITY(Type.VISIBILITY),
	PANIC(Type.POSITIVE),
	// TODO - fear/dread? fatigue?
	// TODO - dampness

	// Modifiers
	MOVEMENT_COST(Type.PERCENTILE),
	// TODO - tracks visibility?

	// Equipment
	ARMOUR(Type.DEFAULT),
	WARMTH(Type.POSITIVE);

	/**
	 * Converter.
	 */
	public static final Converter<EntityValue> CONVERTER = Converter.enumeration(EntityValue.class);

	/**
	 * Primary values.
	 */
	public static final List<EntityValue> PRIMARY_VALUES = List.copyOf(Arrays.stream(EntityValue.values()).filter(EntityValue::isPrimary).collect(toList()));

	/**
	 * Type of value.
	 */
	public enum Type {
		/**
		 * Default value.
		 */
		DEFAULT,

		/**
		 * Primary value.
		 */
		PRIMARY,

		/**
		 * Value representing a 0..100 percentile.
		 */
		PERCENTILE,

		/**
		 * Value that <b>must</b> be positive.
		 */
		POSITIVE,

		/**
		 * Special case for the visibility value.
		 */
		@SuppressWarnings("hiding")
		VISIBILITY
	}

	/**
	 * Key for an entity-value.
	 * @see Type
	 */
	public static final class Key extends AbstractEqualsObject implements ValueModifier.Key {
		/**
		 * Key types.
		 * <p>
		 * A <i>primary</i> entity-value may have any key type, all other values have a type of {@link #DEFAULT}.
		 */
		public enum Type {
			DEFAULT,
			MAXIMUM,
			REGENERATION;

			/**
			 * Key-type converter.
			 */
			@SuppressWarnings("hiding")
			public static final Converter<Type> CONVERTER = Converter.enumeration(Type.class);
		}

		private final EntityValue value;
		private final Type type;

		/**
		 * Constructor.
		 * @param value		Entity-value
		 * @param type		Key type
		 * @throws IllegalArgumentException if the key-type is invalid for the given entity-value
		 */
		private Key(EntityValue value, Type type) {
			this.value = notNull(value);
			this.type = notNull(type);
			if((type != Type.DEFAULT) && !value.isPrimary()) throw new IllegalArgumentException("Invalid key-type for a non-primary value: " + this);
		}

		/**
		 * @return Entity-value
		 */
		public EntityValue value() {
			return value;
		}

		/**
		 * @return Key type
		 */
		public Type type() {
			return type;
		}
	}

	private final Key key = new Key(this, Key.Type.DEFAULT);
	private final Type type;

	/**
	 * Constructor.
	 * @param type Value type
	 */
	private EntityValue(Type type) {
		this.type = notNull(type);
	}

	/**
	 * @return Default key for this entity-value
	 * @see Key.Type#DEFAULT
	 */
	public Key key() {
		return key;
	}

	/**
	 * @return Value type
	 */
	public Type type() {
		return type;
	}

	/**
	 * @return Whether this is a primary value
	 */
	public boolean isPrimary() {
		return type == Type.PRIMARY;
	}

	/**
	 * Creates a entity-value key of the given type.
	 * @param type Key-type
	 * @return Key
	 * @throws IllegalArgumentException if the key-type is invalid for the given entity-value
	 */
	public Key key(Key.Type type) {
		return new Key(this, type);
	}

	/**
	 * @return Mnemonic (or short-name) of this value
	 */
	public String mnemonic() {
		switch(this) {
		case HEALTH:		return "hit";
		case STAMINA:		return "sta";
		case POWER:			return "pow";
		default:			return name().toLowerCase();
		}
	}
}
