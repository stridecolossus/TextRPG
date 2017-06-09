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
			return "equip.put";
			
		case MAIN_HAND:
			return "equip.wield";
			
		case OFF_HAND:
			return "equip.hold";
		
		case KEYRING:
		case BELT:
			return "equip.fasten";
		
		default:
			return "equip.wear";
		}
	}
	
	/**
	 * @return Description key for preposition of this slot
	 */
	public String getPlacement() {
		switch(this) {
		case BACK:
			return "equip.verb.across";
			
		case NECK:
			return "equip.verb.around";

		case WAIST:
			return "equip.verb.about";
			
		case SHOULDER:
			return "equip.verb.over";
				
		case POCKET:
			return "equip.verb.in";
			
		default:
			return "equip.verb.on";
		}
	}
}
