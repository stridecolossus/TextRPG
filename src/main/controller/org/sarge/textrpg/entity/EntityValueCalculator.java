package org.sarge.textrpg.entity;

import java.util.Map;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.ValueModifier;

/**
 * Entity-value calculator.
 * @author Sarge
 */
public class EntityValueCalculator extends AbstractObject {
	private final Map<EntityValue, Calculation> entries;

	/**
	 * Constructor.
	 * @param entries Entries
	 */
	private EntityValueCalculator(Map<EntityValue, Calculation> entries) {
		this.entries = Map.copyOf(entries);
	}

	/**
	 * Calculates an entity-value.
	 * @param key		Key
	 * @param src		Source
	 * @return Value
	 */
	public double calculate(EntityValue key, ValueModifier.Source src) {
		final Calculation value = entries.get(key);
		if(value == null) throw new IllegalArgumentException("Invalid entity-value for calculator: " + key);
		return value.evaluate(src);
	}

	/**
	 * Builder for an entity-value calculator.
	 */
	public static class Builder {
		private final Map<EntityValue, Calculation> map = new StrictMap<>();

		/**
		 * Adds an entity-value calculator entry.
		 * @param key		Key
		 * @param value		Value
		 * @throws IllegalArgumentException if the key is a duplicate
		 * @throws IllegalArgumentException if the key is not a primary entity-value
		 * @see EntityValue#isPrimary()
		 */
		public Builder add(EntityValue key, Calculation value) {
			if(!key.isPrimary()) throw new IllegalArgumentException("Only primary entity-values can be calculated: " + key);
			map.put(key, value);
			return this;
		}

		/**
		 * Constructs this calculator.
		 * @return New calculator
		 * @throws IllegalStateException if the calculator is not complete
		 */
		public EntityValueCalculator build() {
			if(map.size() != 3) throw new IllegalStateException("Incomplete calculator");
			return new EntityValueCalculator(map);
		}
	}
}
