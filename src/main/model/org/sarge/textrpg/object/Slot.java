package org.sarge.textrpg.object;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.CommandArgument;

/**
 * Deployment slots for equipment.
 * @author Sarge
 */
public enum Slot implements CommandArgument {
	NONE,
	HEAD,
	NECK,
	EARRING,
	BODY,
	BACK,
	SHOULDER,
	POCKET,
	ARMS,
	HANDS,
	MAIN,
	OFF,
	RING,
	WAIST,
	BELT,
	KEYRING,
	LEGS,
	FEET;

	/**
	 * Converter.
	 */
	public static final Converter<Slot> CONVERTER = Converter.enumeration(Slot.class);

	/**
	 * @return Whether this slot requires an equipped container (such as a key-ring for keys)
	 */
	public boolean isContainer() {
		switch(this) {
		case BELT:
		case KEYRING:
			return true;

		default:
			return false;
		}
	}

	/**
	 * @return Whether this is a <i>hands</i> slot
	 */
	public boolean isHanded() {
		return (this == MAIN) || (this == OFF);
	}

	/**
	 * @return Verb used to deploy to this slot
	 */
	public String verb() {
		switch(this) {
		case BACK:
		case SHOULDER:
		case POCKET:
		case RING:
			return "put";

		case MAIN:
			return "wield";

		case KEYRING:
		case BELT:
			return "fasten";

		default:
			return "wear";
		}
	}

	/**
	 * @return Placement of this slot
	 */
	public String placement() {
		switch(this) {
		case BACK:
			return "across";

		case NECK:
			return "around";

		case WAIST:
		case BODY:
			return "about";

		case SHOULDER:
			return "over";

		case POCKET:
			return "in";

		case BELT:
			return "as";

		default:
			return "on";
		}
	}
}
