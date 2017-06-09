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
	URBAN_LIT,
	INDOORS,
	INDOORS_DARK,
	UNDERGROUND,
	WATER;
	
	/**
	 * @return Whether this terrain is naturally dark
	 */
	public boolean isDark() {
		switch(this) {
		case INDOORS_DARK:
		case UNDERGROUND:
			return true;
			
		default:
			return false;
		}
	}
	
	/**
	 * @return Whether this terrain is naturally lit
	 */
	public boolean isLit() {
		switch(this) {
		case INDOORS:
		case URBAN_LIT:
			return true;
			
		default:
			return false;
		}
	}
}
