package org.sarge.textrpg.object;

/**
 * Object cardinality.
 * @author Sarge
 */
public enum Cardinality {
	/**
	 * Single object, e.g. <b>a</b> book.
	 */
	SINGLE,

	/**
	 * Group of objects, e.g. <b>some</b> beans.
	 */
	SOME,

	/**
	 * Pair of objects, e.g. a <b>pair</b> of trousers.
	 */
	PAIR,

	/**
	 * Named object, e.g. <b>The</b> Doors of Moria.
	 */
	UNIQUE
}
