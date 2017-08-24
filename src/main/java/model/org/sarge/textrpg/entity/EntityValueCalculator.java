package org.sarge.textrpg.entity;

import java.util.Map;

import org.sarge.lib.collection.Pair;
import org.sarge.lib.collection.StrictMap;

/**
 * Helper that initialises entity values.
 * @author Sarge
 */
public class EntityValueCalculator {
	private final Map<Pair<EntityValue, Attribute>, Float> modifiers = new StrictMap<>();

	/**
	 * Adds a modifier for an entity-value as an attribute score scaled by the given modifier.
	 * @param value		Entity-value
	 * @param attr		Attribute
	 * @param mod		Modifier
	 */
	public void add(EntityValue value, Attribute attr, float mod) {
		if(!value.getMaximumValue().isPresent()) throw new IllegalArgumentException("Can only initialise non-maximum values");
		final Pair<EntityValue, Attribute> key = new Pair<>(value, attr);
		modifiers.put(key, mod);
	}
	
	/**
	 * Initialises the transient values for the given entity.
	 * @param e Entity to initialise
	 */
	public void init(Entity e) {
		// Initialise maximum values
		for(Pair<EntityValue, Attribute> key : modifiers.keySet()) {
			init(e, key);
		}
		
		// Copy values from maximums
		modifiers.keySet().stream()
			.map(key -> key.getLeft())
			.distinct()
			.forEach(val -> init(e, val));
		
		// TODO
		e.modify(EntityValue.HUNGER, 25);
		e.modify(EntityValue.THIRST, 25);
	}

	/**
	 * Increments an entity value based on an attribute score.
	 * @param e			Entity
	 * @param value		Entity-value
	 * @param attr		Attribute
	 */
	private void init(Entity e, Pair<EntityValue, Attribute> key) {
		final float mod = modifiers.get(key);
		final int num = e.getAttributes().get(key.getRight());
		e.modify(key.getLeft(), (int) (num * mod));
	}
	
	/**
	 * Initialises an entity-value to its associated maximum.
	 * @param e			Entity
	 * @param value		Entity-value
	 */
	private static void init(Entity e, EntityValue value) {
		final EntityValue max = value.getMaximumValue().orElseThrow(() -> new IllegalArgumentException("Cannot initialise " + value + " from maximum"));
		final int num = e.getValues().get(value);
		assert e.getValues().get(max) == 0;
		e.modify(max, num);
	}
	
	@Override
	public String toString() {
		return modifiers.toString();
	}
}
