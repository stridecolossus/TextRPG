package org.sarge.textrpg.util;

/**
 * A <i>value modifier</i> abstracts over a entity property that can be modified.
 * @author Sarge
 */
public interface ValueModifier {
	/**
	 * @return Value
	 */
	int get();

	/**
	 * Modifies this value.
	 * @param mod Modification
	 * @return New value
	 */
	int modify(float value);

	/**
	 * Marker interface for TODO
	 */
	interface Key {
		// Marker interface
	}

	/**
	 * Source of value modifiers.
	 */
	interface Source {
		/**
		 * Looks up the value-modifier for the given key.
		 * @param key Modifier key
		 * @return Modifier
		 */
		ValueModifier modifier(Key key);
	}
}
