package org.sarge.textrpg.object;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;

/**
 * Set of contents that tracks total weight.
 * @author Sarge
 */
public class TrackedContents extends Contents {
	/**
	 * Defines a limit for a container.
	 */
	public interface Limit {
		/**
		 * Tests the given object can be added to this set of contents.
		 * @param obj			Object being added
		 * @param contents		Contents
		 * @return Whether object exceeds this limit
		 */
		boolean exceeds(Thing obj, TrackedContents contents);

		/**
		 * Creates a number-based limit.
		 * @param max Maximum number of objects in this container
		 * @return Number limit
		 */
		static Limit number(int max) {
			Check.zeroOrMore(max);
			return (obj, c) -> c.size() >= max;
		}

		/**
		 * Creates a weight-based limit.
		 * @param max Maximum total weight of the contents of the container
		 * @return Weight limit
		 */
		static Limit weight(int max) {
			Check.zeroOrMore(max);
			return (obj, c) -> c.getWeight() + obj.weight() > max;
		}

		/**
		 * Creates a size-based limit.
		 * @param max Maximum size of an object allowed in this container
		 * @return Size limit
		 */
		static Limit size(Size max) {
			Check.notNull(max);
			return (obj, c) -> obj.getSize().isLargerThan(max);
		}
	}

	private final Map<Limit, String> limits;

	private int weight;

	/**
	 * Default constructor for contents with no limits.
	 */
	public TrackedContents() {
		this(Collections.emptyMap());
	}

	/**
	 * Constructor for a limited set of contents.
	 * @param limits Limit(s) mapped to reason code
	 */
	public TrackedContents(Map<Limit, String> limits) {
		this.limits = new HashMap<>(limits);
	}

	/**
	 * @return Total weight of this set of contents
	 */
	@Override
	public int getWeight() {
		return weight;
	}

	@Override
	public String getReason(Thing obj) {
		final Optional<Limit> exceeded = limits.keySet().stream().filter(f -> f.exceeds(obj, this)).findFirst();
		return exceeded.map(limits::get).map(reason -> "contents.add." + reason).orElseGet(() -> super.getReason(obj));
	}

	@Override
	public void add(Thing obj) {
		super.add(obj);
		weight += obj.weight();
	}

	@Override
	public void remove(Thing obj) {
		super.remove(obj);
		weight -= obj.weight();
	}
}
