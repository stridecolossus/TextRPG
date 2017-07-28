package org.sarge.textrpg.object;

/**
 * Deployment slots for equipment.
 * @author Sarge
 */
public enum DeploymentSlot {
	HEAD,
	NECK,
	EARRING,
	BODY,
	BACK,
	SHOULDER,
	POCKET,
	ARMS,
	HANDS,
	MAIN_HAND,
	OFF_HAND,
	RING,
	WAIST,
	BELT,
	KEYRING,
	LEGS,
	FEET;

	/**
	 * @return Whether this slot requires an equipped container (such as a key-ring for keys)
	 */
	public boolean isContainerSlot() {
		switch(this) {
		case BELT:
		case KEYRING:
			return true;

		default:
			return false;
		}
	}

	/**
	 * @return Description key for the verb associated with this slot
	 */
	public String getVerb() {
		switch(this) {
		case BACK:
		case SHOULDER:
		case POCKET:
		case RING:
			return "put";

		case MAIN_HAND:
			return "wield";

		case OFF_HAND:
			return "hold";

		case KEYRING:
		case BELT:
			return "fasten";

		default:
			return "wear";
		}
	}

	/**
	 * @return Description key for preposition of this slot
	 */
	public String getPlacement() {
		switch(this) {
		case BACK:
			return "across";

		case NECK:
			return "around";

		case WAIST:
			return "about";

		case SHOULDER:
			return "over";

		case POCKET:
			return "in";

		default:
			return "on";
		}
	}
}
