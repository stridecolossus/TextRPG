package org.sarge.textrpg.contents;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;
import java.util.Optional;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Size;

/**
 * A set of contents with constraints on which objects can be added.
 * @author Sarge
 */
public class LimitedContents extends TrackedContents {
	/**
	 * Limit applies to a {@link LimitedContents}.
	 */
	@FunctionalInterface
	public interface Limit {
		/**
		 * Tests whether a set of contents can accept the given thing.
		 * @param contents		Contents
		 * @param thing			Thing to add
		 * @return Whether the contents can accept the given thing
		 */
		boolean accepts(Contents contents, Thing thing);

		/**
		 * Creates a limit on the maximum number of objects that can be added to a set of contents.
		 * @param max Maximum number of contents
		 * @return Number of contents limit
		 * @see Contents#size()
		 */
		static Limit capacity(int max) {
			return (contents, thing) -> contents.size() < max;
		}

		/**
		 * Creates a limit on the maximum size of objects added to a set of contents.
		 * @param max Maximum size of contents
		 * @return Contents size limit
		 * @see Thing#size()
		 */
		static Limit size(Size max) {
			return (contents, thing) -> !max.isLessThan(thing.size());
		}

		/**
		 * Creates a limit on the total combined weight of a set of contents.
		 * @param max Maximum total weight
		 * @return Total weight limit
		 * @see Thing#weight()
		 * @see Contents#weight()
		 */
		static Limit weight(int max) {
			return (contents, thing) -> contents.weight() + thing.weight() <= max;
		}
	}

	/**
	 * Immutable set of limits and associated reason codes.
	 */
	public static final class LimitsMap {
		/**
		 * Empty set of limits.
		 */
		public static final LimitsMap EMPTY = new LimitsMap();

		private final Map<String, Limit> map;

		/**
		 * Constructor.
		 * @param limits Limits ordered by reason code
		 * @throws IllegalArgumentException if the given map is empty or contains any duplicates
		 */
		public LimitsMap(Map<String, Limit> limits) {
			Check.notEmpty(limits);
			this.map = Map.copyOf(new StrictMap<>(limits));
		}

		/**
		 * Empty constructor.
		 */
		private LimitsMap() {
			map = Map.of();
		}

		@Override
		public String toString() {
			return map.toString();
		}
	}

	private final LimitsMap limits;

	/**
	 * Constructor.
	 * @param limits Limits with associated reason codes
	 */
	public LimitedContents(LimitsMap limits) {
		this.limits = notNull(limits);
	}

	@Override
	public Optional<String> reason(Thing thing) {
		return limits.map.entrySet().stream()
			.filter(entry -> !entry.getValue().accepts(this, thing))
			.map(Map.Entry::getKey)
			.findAny()
			.or(() -> EMPTY_REASON);
	}
}
