package org.sarge.textrpg.world;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.util.ModelUtil;

/**
 * Compass and movement directions.
 * @author Sarge
 */
public enum Direction {
	NORTH,
	SOUTH,
	EAST,
	WEST,
	UP,
	DOWN;
	
	private final String mnenomic;
	
	private Direction() {
		this.mnenomic = Character.toString(Character.toLowerCase(this.name().charAt(0)));
	}

	/**
	 * @return Reverse of this direction
	 */
	public Direction reverse() {
		switch(this) {
		case NORTH:		return SOUTH;
		case SOUTH:		return NORTH;
		case EAST:		return WEST;
		case WEST:		return EAST;
		case UP:		return DOWN;
		case DOWN:		return UP;
		default: throw new RuntimeException();
		}
	}

	/**
	 * @return Mnenomic (or short) name of this direction
	 */
	public String getMnenomic() {
		return mnenomic;
	}

	/**
	 * @return Direction converter
	 * @see #getMnenomic()
	 */
	public static final Converter<Direction> CONVERTER = ModelUtil.converter(Direction.class, Direction::getMnenomic);
}
