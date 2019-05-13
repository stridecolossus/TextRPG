package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.StreamUtil;

/**
 * An <i>exit map</i> is a set of exits from a location indexed by direction.
 */
public interface ExitMap {
	/**
	 * Empty exit.
	 */
	Optional<Exit> EMPTY_EXIT = Optional.empty();

	/**
	 * Empty set of exits, e.g. for an orphaned location.
	 */
	ExitMap EMPTY = new Empty();

	/**
	 * @return Whether this set of exits is empty
	 */
	boolean isEmpty();

	/**
	 * @return All exits as a stream
	 */
	Stream<Exit> stream();

	/**
	 * Helper - Finds the exit in the given direction.
	 * @param dir Direction
	 * @return Exit descriptor
	 */
	Optional<Exit> find(Direction dir);

	/**
	 * Creates an exit-map for a single entry.
	 * @param exit Exit
	 * @return Single exit-map
	 */
	static ExitMap of(Exit exit) {
		return new SingleExitMap(exit);
	}

	/**
	 * Empty set of exits.
	 */
	class Empty implements ExitMap {
		private Empty() {
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Optional<Exit> find(Direction dir) {
			return EMPTY_EXIT;
		}

		@Override
		public Stream<Exit> stream() {
			return Stream.empty();
		}
	}

	/**
	 * Single exit.
	 */
	class SingleExitMap extends AbstractEqualsObject implements ExitMap {
		private final Exit exit;

		/**
		 * Constructor.
		 * @param exit Single exit
		 */
		private SingleExitMap(Exit exit) {
			this.exit = notNull(exit);
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Stream<Exit> stream() {
			return Stream.of(exit);
		}

		@Override
		public Optional<Exit> find(Direction dir) {
			if(dir == exit.direction()) {
				return Optional.of(exit);
			}
			else {
				return EMPTY_EXIT;
			}
		}
	}


	/**
	 * Exits stored as an array indexed by the {@link Direction} ordinal.
	 */
	class ArrayExitMap extends AbstractEqualsObject implements ExitMap {
		private final Exit[] exits;

		/**
		 * Constructor.
		 * @param len		Array length
		 * @param array 	Exits
		 */
		protected ArrayExitMap(int len, Exit[] exits) {
			assert exits.length > 0;
			this.exits = new Exit[len];
			for(Exit e : exits) {
				final int index = e.direction().ordinal();
				this.exits[index] = e;
			}
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

	/**
	 * Mutable implementation.
	 */
	class MutableExitMap extends AbstractEqualsObject implements ExitMap {
		private final Map<Direction, Exit> exits = new StrictMap<>();

		@Override
		public boolean isEmpty() {
			return exits.isEmpty();
		}

		@Override
		public Stream<Exit> stream() {
			return exits.values().stream();
		}

		@Override
		public Optional<Exit> find(Direction dir) {
			return Optional.ofNullable(exits.get(dir));
		}

		/**
		 * Adds an exit.
		 * @param exit Exit
		 * @throws IllegalArgumentException for a duplicate direction
		 */
		public MutableExitMap add(Exit exit) {
			verify(exit);
			exits.put(exit.direction(), exit);
			return this;
		}

		private void verify(Exit exit) {
			if(exit.link() instanceof CurrentLink) {
				if(StreamUtil.select(CurrentLink.class, exits.values().stream().map(Exit::link)).count() > 0) {
					throw new IllegalArgumentException("Location can only have one current link");
				}
			}
		}

		/**
		 * Creates a specialised immutable implementation from this exit-map.
		 * @return New immutable exit-map
		 */
		public ExitMap compact() {
			switch(exits.size()) {
			case 0:
				// Empty map
				return EMPTY;

			case 1:
				// Single exit
				return new SingleExitMap(exits.values().iterator().next());

			default:
				// Multiple exits
				final Exit[] array = exits.values().toArray(Exit[]::new);
				if(isCardinal(array)) {
					return new ArrayExitMap(4, array);
				}
				else {
					return new ArrayExitMap(6, array);
				}
			}
		}

		/**
		 * @return Whether the given exits are <b>all</b> default links in the cardinal directions
		 * @see Direction#isCardinal()
		 */
		private static boolean isCardinal(Exit[] exits) {
			if(exits.length != 4) {
				return false;
			}
			else {
				return Arrays.stream(exits)
					.filter(e -> e.link() == Link.DEFAULT)
					.map(Exit::direction)
					.allMatch(Direction::isCardinal);
			}
		}
	}
}
