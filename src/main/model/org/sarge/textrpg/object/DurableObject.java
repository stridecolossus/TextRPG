package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.Percentile;

/**
 * A <i>durable</i> object sustains wear during use and can be damaged in combat, e.g. armour, tools, etc.
 * @author Sarge
 */
public class DurableObject extends WorldObject {
	/**
	 * Descriptor for a durable object.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final int max;

		/**
		 * Constructor.
		 * @param descriptor 		Object descriptor
		 * @param max				Maximum wear
		 */
		public Descriptor(ObjectDescriptor descriptor, int max) {
			super(descriptor);
			this.max = oneOrMore(max);
		}

		/**
		 * Sub-class copy constructor.
		 * @param descriptor Durable descriptor
		 */
		protected Descriptor(Descriptor descriptor) {
			super(descriptor);
			this.max = descriptor.max;
		}

		@Override
		public DurableObject create() {
			return new DurableObject(this);
		}
	}

	private int wear;

	/**
	 * Constructor.
	 * @param descriptor Durable object descriptor
	 */
	protected DurableObject(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	/**
	 * @return Amount of wear sustained by this object
	 */
	public int wear() {
		return wear;
	}

	/**
	 * @return Condition of this object (inverted)
	 */
	public Percentile condition() {
		final int max = this.descriptor().max;
		return Percentile.of(wear, max);
	}

	@Override
	public boolean isDamaged() {
		return wear > 0;
	}

	@Override
	public boolean isBroken() {
		final int max = this.descriptor().max;
		return wear == max;
	}

	@Override
	public void use() {
		if(isBroken()) throw new IllegalStateException("Cannot use a broken object");
		++wear;
	}

	@Override
	protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
		if(carried) {
			builder.add(KEY_CONDITION, condition(), formatters.get("durable.condition"));
		}
		super.describe(carried, builder, formatters);
	}

	/**
	 * Repairs this durable object.
	 * @throws IllegalStateException if this object is not damaged
	 */
	void repair() {
		if(!isDamaged()) throw new IllegalStateException("Not damaged");
		wear = 0;
	}

	/**
	 * Partially repairs this object.
	 * @param amount Amount of wear to repair
	 * @throws IllegalStateException if this object is not damaged
	 */
	void repair(int amount) {
		if(!isDamaged()) throw new IllegalStateException("Not damaged");
		wear = Math.max(wear - amount, 0);
	}
}
