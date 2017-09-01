package org.sarge.textrpg.world;

/**
 * Terrain types.
 * @author Sarge
 */
public enum Terrain {
	GRASSLAND,
	FARMLAND,
	SCRUBLAND,
	HILL,
	MOUNTAIN,
	FOREST,
	WOODLAND,
	JUNGLE,
	MARSH,
	DESERT,
	ICE,
	SNOW,
	URBAN,
	INDOORS,
	UNDERGROUND,
	WATER;

	/**
	 * @return Whether this terrain is naturally dark irrespective of the time-of-day
	 */
	public boolean isDark() {
		switch(this) {
		case INDOORS:
		case UNDERGROUND:
			return true;

		default:
			return false;
		}
	}
}
