package org.sarge.textrpg.world;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Arbitrary map of exits stored as an array indexed by the {@link Direction} ordinal.
 */
class ArrayExitMap extends AbstractEqualsObject implements ExitMap {
	private static final Direction[] DIRECTIONS = Direction.values().clone();

	private final Exit[] exits = new Exit[DIRECTIONS.length];

	/**
	 * Constructor.
	 * @param map Exit map
	 * @throws IllegalArgumentException if the given exits are empty
	 */
	ArrayExitMap(ExitMap map) {
		if(map.isEmpty()) throw new IllegalArgumentException("Empty map");
		map.stream().forEach(exit -> exits[exit.direction().ordinal()] = exit);
		// TODO - store just destination if Link.DEFAULT
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Stream<Exit> stream() {
		return Arrays.stream(exits).filter(Objects::nonNull);
	}

	@Override
	public Optional<Exit> find(Direction dir) {
		return Optional.ofNullable(exits[dir.ordinal()]);
	}
}
