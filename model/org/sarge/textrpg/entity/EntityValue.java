package org.sarge.textrpg.entity;

import java.util.Optional;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.util.ModelUtil;

/**
 * Entity transient values.
 * @author Sarge
 */
public enum EntityValue {
	// Main
	MAX_HEALTH,
	HEALTH(MAX_HEALTH),
	MAX_POWER,
	POWER(MAX_POWER),
	MAX_STAMINA,
	STAMINA(MAX_STAMINA),
	
	// Cumulative
	HUNGER,
	THIRST,
	
	// Modifiers
	MOVE_COST,
	
	// Miscellaneous
	ARMOUR,
	CASH,
	EXPERIENCE,
	POINTS;
	
	private final Optional<EntityValue> max;

	private EntityValue(EntityValue max) {
		this.max = Optional.ofNullable(max);
	}

	private EntityValue() {
		this.max = Optional.empty();
	}

	/**
	 * @return Maximum of this value
	 */
	public Optional<EntityValue> getMaximumValue() {
		return max;
	}
	
	/**
	 * @return Short-name
	 */
	public String getShortName() {
		switch(this) {
		case HEALTH:		return "hit";
		case POWER:			return "pow";
		case STAMINA:		return "sta";
		case ARMOUR:		return "arm";
		case MOVE_COST:		return "move";
		default:			return name();
		}
	}
	
	/**
	 * @return Converter
	 * @see #getShortName()
	 */
	public static final Converter<EntityValue> CONVERTER = ModelUtil.converter(EntityValue.class, EntityValue::getShortName);
}
