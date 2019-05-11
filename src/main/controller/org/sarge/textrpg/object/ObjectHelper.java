package org.sarge.textrpg.object;

/**
 * Object utilities.
 * @author Sarge
 */
public final class ObjectHelper {
	/**
	 * Destroys the given object.
	 * @param obj Object to destroy
	 * @see WorldObject#destroy()
	 */
	public static void destroy(WorldObject obj) {
		obj.destroy();
	}
}
// TODO - remove this
