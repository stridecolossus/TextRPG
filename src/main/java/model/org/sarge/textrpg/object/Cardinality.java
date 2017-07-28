package org.sarge.textrpg.object;

import org.sarge.textrpg.common.Description;

/**
 * Object cardinality.
 * @author Sarge
 */
public enum Cardinality {
	/**
	 * Single object, e.g. <b>a</b> sword
	 */
	SINGLE,

	/**
	 * Pair of objects, e.g. <b>a pair</b> of trousers
	 */
	PAIR,

	/**
	 * A set of objects, e.g. <b>some</b> beans
	 */
	SOME,

	/**
	 * Unique object, e.g. <b>The</b> Doors of Moria
	 */
	UNIQUE;

	/**
	 * Helper - Adds a wrapped cardinality attribute to the given description.
	 * @param builder Description
	 */
	public void add(Description.Builder builder) {
		builder.wrap("cardinality", "cardinality", this.name());
	}
}
