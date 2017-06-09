package org.sarge.textrpg.entity;

/**
 * Effect applied to an entity.
 * @author Sarge
 */
public interface EffectMethod {
	/**
	 * Applies this effect to the given entity.
	 * @param e			Entity
	 * @param size		Size of effect
	 */
	void apply(Entity e, int size);
	
	/**
	 * @return Whether this is wounding effect
	 */
	default boolean isWound() {
		return false;
	}

	/**
	 * Creates an attribute effect.
	 * @param attr Attribute
	 * @return Attribute effect
	 */
	static EffectMethod attribute(Attribute attr) {
		return (e, size) -> e.modify(attr, size);
	}
	
	/**
	 * Creates an entity-value effect.
	 * @param value Entity-value
	 * @return Entity-value effect
	 */
	static EffectMethod value(EntityValue value) {
		return (e, size) -> e.modify(value, size);
	}
}
