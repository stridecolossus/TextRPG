package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;

/**
 * A <i>receptacle</i> is a container for a {@link Liquid}.
 * @author Sarge
 */
public class Receptacle extends WorldObject {
	/**
	 * Level for an infinite receptacle.
	 */
	public static final int INFINITE = Integer.MAX_VALUE;

	/**
	 * Descriptor for a receptacle.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Liquid liquid;
		private final int level;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param liquid			Liquid
		 * @param level				Initial/maximum level or {@link Receptacle#INFINITE} for an infinite receptacle
		 */
		public Descriptor(ObjectDescriptor descriptor, Liquid liquid, int level) {
			super(descriptor);
			this.liquid = notNull(liquid);
			this.level = oneOrMore(level);
		}

		/**
		 * @return Liquid contained by this receptacle
		 */
		public Liquid liquid() {
			return liquid;
		}

		@Override
		public Receptacle create() {
			return new Receptacle(this);
		}
	}

	private int level;

	/**
	 * Constructor.
	 * @param descriptor Receptacle descriptor
	 */
	protected Receptacle(Descriptor descriptor) {
		super(descriptor);
		this.level = descriptor.level;
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	@Override
	public int weight() {
		return super.weight() + level;
	}

	/**
	 * @return Liquid level of this receptacle
	 */
	public int level() {
		return level;
	}

	/**
	 * @return Whether this receptacle is empty
	 */
	public boolean isEmpty() {
		return level == 0;
	}

	@Override
	protected void describe(boolean carried, Description.Builder builder, ArgumentFormatter.Registry formatters) {
		if(carried) {
			final Descriptor descriptor = descriptor();
			final Percentile arg = Percentile.of(level, descriptor.level);
			builder.add("level", arg, formatters.get("receptacle.level"));
		}
		super.describe(carried, builder, formatters);
	}

	/**
	 * Consumes some or all of the contents of this receptacle.
	 * @param amount Amount to consume
	 * @return Actual amount consumed
	 * @throws IllegalArgumentException if this receptacle is empty
	 */
	public int consume(int amount) {
		if(level == INFINITE) {
			return amount;
		}
		else {
			if(level == 0) throw new IllegalArgumentException("Receptacle is empty");
			final int actual = Math.min(level, amount);
			level -= actual;
			return actual;
		}
	}

	/**
	 * Empties this receptacle.
	 * @throws ActionException if this receptacle is already empty or is {@link #INFINITE}
	 */
	void empty() throws ActionException {
		if(level == 0) throw ActionException.of("receptacle.already.empty");
		if(level == INFINITE) throw ActionException.of("receptacle.empty.infinite");
		level = 0;
	}

	/**
	 * Fills this receptacle from the given source.
	 * @param src Source receptacle
	 * @throws ActionException if this receptacle is already full, the source is empty, this is an {@link #INFINITE} receptacle, or the liquids are different
	 */
	void fill(Receptacle src) throws ActionException {
		// Check levels
		final Descriptor descriptor = this.descriptor();
		if(level == INFINITE) throw ActionException.of("receptacle.fill.infinite");
		if(level == descriptor.level) throw ActionException.of("receptacle.already.full");
		if(src.level == 0) throw ActionException.of("receptacle.source.empty");
		if(src == this) throw new IllegalArgumentException("Cannot fill from self");

		// Check same liquid
		final Descriptor that = src.descriptor();
		if(descriptor.liquid != that.liquid) throw ActionException.of("receptacle.fill.invalid");

		if(that.level == INFINITE) {
			// Fill from infinite source
			this.level = descriptor.level;
		}
		else {
			// Fill from source
			final int actual = Math.min(descriptor.level - level, src.level);
			src.level -= actual;
			this.level += actual;
		}
	}
}
