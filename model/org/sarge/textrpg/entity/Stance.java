package org.sarge.textrpg.entity;

/**
 * Entity stances.
 * @author Sarge
 */
public enum Stance {
	DEFAULT,
	RESTING,
	SLEEPING,
	COMBAT,
	SNEAKING,
	MOUNTED;
	
	/**
	 * Verifies a stance transition.
	 * @param stance Current stance
	 * @return Whether this stance is a valid transition <b>from</b> the given stance
	 */
	public boolean isValidTransition(Stance stance) {
		switch(this) {
		case MOUNTED:
		case SNEAKING:
			return stance == DEFAULT;

		case RESTING:
			return (stance == DEFAULT) || (stance == SLEEPING);
			
		case SLEEPING:
			return (stance == DEFAULT) || (stance == RESTING);

		default:
			return true;
		}
	}
}
