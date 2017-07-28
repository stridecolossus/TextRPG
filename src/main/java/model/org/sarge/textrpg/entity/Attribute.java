package org.sarge.textrpg.entity;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.util.ModelUtil;

/**
 * Entity attributes.
 * @author Sarge
 */
public enum Attribute {
	STRENGTH,
	ENDURANCE,
	AGILITY,
	INTELLIGENCE,
	WILL,
	SKILL,
	PERCEPTION;

	/**
	 * @return Short-name
	 */
	public String getMnemonic() {
		return this.name().substring(0, 3).toLowerCase();
	}

	/**
	 * @return Attribute converter
	 * @see #getMnemonic()
	 */
	public static final Converter<Attribute> CONVERTER = ModelUtil.converter(Attribute.class, Attribute::getMnemonic);
}
