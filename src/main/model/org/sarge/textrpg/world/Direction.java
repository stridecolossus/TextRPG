package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.util.MnemonicConverter;

/**
 * Compass and movement directions.
 * @author Sarge
 */
public enum Direction implements CommandArgument {
	NORTH,
	SOUTH,
	EAST,
	WEST,
	UP,
	DOWN;

	private final String mnemonic;

	private Direction() {
		this.mnemonic = Character.toString(Character.toLowerCase(this.name().charAt(0)));
	}

	/**
	 * @return Direction converter
	 * @see #mnemonic()
	 */
	public static final Converter<Direction> CONVERTER = MnemonicConverter.converter(Direction.class, Direction::mnemonic);

	/**
	 * Creates a direction path from the given string.
	 * @param path Path string
	 * @return Path
	 */
	public static List<Direction> path(String path) {
		return path.chars().mapToObj(ch -> String.valueOf((char) ch)).map(Direction.CONVERTER).collect(toList());
	}

	/**
	 * @return Mnemonic (or short) name of this direction
	 */
	public String mnemonic() {
		return mnemonic;
	}

	/**
	 * @return Whether this is a cardinal (or compass) direction
	 */
	public boolean isCardinal() {
		switch(this) {
		case NORTH:
		case SOUTH:
		case EAST:
		case WEST:
			return true;

		default:
			return false;
		}
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
}
