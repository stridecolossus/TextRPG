package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.StreamUtil;

/**
 * Set of exits indexed by direction.
 */
public interface ExitMap {
	/**
	 * Empty exit.
	 */
	Optional<Exit> EMPTY_EXIT = Optional.empty();

	/**
	 * @return Whether this set of exits is empty
	 */
	boolean isEmpty();

	/**
	 * Finds the exit in the given direction.
	 * @param dir Direction
	 * @return Exit descriptor
	 */
	Optional<Exit> find(Direction dir);

	/**
	 * @return All exits as a stream
	 */
	Stream<Exit> stream();

	/**
	 * Adds an exit.
	 * @param exit Exit
	 * @throws UnsupportedOperationException by default
	 */
	default void add(Exit exit) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Empty set of exits, e.g. for an orphaned location.
	 */
	ExitMap EMPTY = new Empty();

	/**
	 * Creates an exits-map with a single entry.
	 * @param exit Exit
	 * @return Exits map
	 * @see SingleExitMap
	 */
	static ExitMap of(Exit exit) {
		return new SingleExitMap(exit);
	}

	/**
	 * Creates a specialised exit map implementation for the given set of exits. The resultant map may be immutable.
	 * @param map Exit map
	 * @return New exit map
	 */
	static ExitMap of(ExitMap map) {
		if(map instanceof MutableExitMap) {
			final MutableExitMap mutable = (MutableExitMap) map;
			switch(mutable.exits.size()) {
			case 0:
				// Empty map
				return EMPTY;

			case 1:
				// Single exit
				return new SingleExitMap(mutable.exits.values().iterator().next());

			default:
				// Multiple exits
				return new ArrayExitMap(mutable);
			}
		}
		else {
			return map;
		}
	}

	/**
	 * Empty set of exits.
	 */
	class Empty implements ExitMap {
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
		public SingleExitMap(Exit exit) {
			this.exit = notNull(exit);
		}

		@Override
		public boolean isEmpty() {
			return false;
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

		@Override
		public Stream<Exit> stream() {
			return Stream.of(exit);
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

		@Override
		public void add(Exit exit) {
			verify(exit);
			exits.put(exit.direction(), exit);
		}

		private void verify(Exit exit) {
			if(exit.link() instanceof CurrentLink) {
				if(StreamUtil.select(CurrentLink.class, exits.values().stream().map(Exit::link)).count() > 0) {
					throw new IllegalArgumentException("Location can only have one current link");
				}
			}
		}
	}
}
