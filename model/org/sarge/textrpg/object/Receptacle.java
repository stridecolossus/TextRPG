package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Description;

/**
 * Receptacle for a {@link Liquid} such as an oil flask, water-skin, well or potion.
 * @author Sarge
 */
public class Receptacle extends WorldObject {
	/**
	 * Receptacle descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Liquid liquid;
		private final int max;
		private final boolean potion;

		/**
		 * Constructor.
		 * @param descriptor	Object descriptor
		 * @param liquid		Liquid descriptor
		 * @param max			Initial/maximum level
		 * @param potion		Whether this receptacle is a potion
		 */
		public Descriptor(ObjectDescriptor descriptor, Liquid liquid, int max, boolean potion) {
			super(descriptor);
			Check.notNull(liquid);
			Check.oneOrMore(max);
			this.liquid = liquid;
			this.max = max;
			this.potion = potion;
		}
		
		@Override
		public boolean isTransient() {
			return max < INFINITE;
		}
		
		@Override
		public String getDescriptionKey() {
			return "receptacle";
		}
		
		/**
		 * @return Receptacle liquid
		 */
		public Liquid getLiquid() {
			return liquid;
		}
		
		/**
		 * @return Whether this receptacle is a potion
		 */
		public boolean isPotion() {
			return potion;
		}
		
		@Override
		public Receptacle create() {
			return new Receptacle(this);
		}
	}

	/**
	 * Level for an infinite receptacle.
	 */
	public static final int INFINITE = Integer.MAX_VALUE;
	
	/**
	 * Global water receptacle.
	 */
	public static final Receptacle WATER = new Receptacle(new Descriptor(new ObjectDescriptor("water"), Liquid.WATER, INFINITE, false));
	
	private int level;
	
	/**
	 * Constructor.
	 * @param descriptor Receptacle descriptor
	 */
	public Receptacle(Descriptor descriptor) {
		super(descriptor);
		this.level = descriptor.max;
	}
	
	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.getDescriptor();
	}
	
	/**
	 * @return Liquid level in this receptacle
	 */
	public int getLevel() {
		return level;
	}
	
	@Override
	public int getWeight() {
		return super.getWeight() + level;
	}
	
	@Override
	protected void describe(Description.Builder description) {
		description.add("level", level);
		description.add("liquid", getDescriptor().liquid);
	}

	/**
	 * Fills this receptacle from the given source.
	 * @param src Source receptacle
	 * @throws ActionException if this object is already full or is an infinite receptacle, or the source does not match this receptacle or is empty
	 */
	protected void fill(Receptacle src) throws ActionException {
		// Check this receptacle can be filled from the given source
		final Descriptor rec = getDescriptor();
		if(rec.liquid != src.getDescriptor().getLiquid()) throw new ActionException("fill.invalid.source");
		if(rec.max == INFINITE) throw new ActionException("fill.infinite.receptacle");
		if(src == this) throw new ActionException("fill.self");
		if(this.level == rec.max) throw new ActionException("fill.already.full");
		if(src.level == 0) throw new ActionException("fill.empty.source");
		
		if(src.level == INFINITE) {
			// Fill completely from infinite source
			this.level = rec.max;
		}
		else {
			// Otherwise consume available from source
			final int amount = Math.min(src.level, rec.max - this.level);
			this.level += amount;
			src.level -= amount;
			assert src.validate();
		}
		assert this.validate();
	}

	/**
	 * Consumes some or all of the contents of this receptacle.
	 * @param amount Amount to consume
	 * @return Actual amount consumed
	 * @throws ActionException if this receptacle is empty
	 * @throws IllegalArgumentException if this receptacle is a potion and the amount is not <b>exactly one</b> unit
	 */
	protected int consume(int amount) throws ActionException {
		if(this.level == 0) throw new ActionException("receptacle.consume.empty");
		if(getDescriptor().potion && (amount != 1)) throw new IllegalArgumentException("Can only consume one unit of a potion");
		if(this.level == INFINITE) {
			return amount;
		}
		else {
			final int actual = Math.min(amount, this.level);
			this.level -= actual;
			assert this.validate();
			return actual;
		}
	}
	
	/**
	 * Empties this receptacle.
	 * @throws ActionException if this receptacle is already empty or is infinite
	 */
	protected void empty() throws ActionException {
		if(this.level == 0) throw new ActionException("receptacle.already.empty");
		if(this.level == INFINITE) throw new ActionException("receptacle.empty.infinite");
		this.level = 0;
	}

	/**
	 * Validates the level of this receptacle.
	 */
	private boolean validate() {
		return (this.level >= 0) && (this.level <= getDescriptor().max);
	}
	
	@Override
	protected void destroy() {
		this.level = 0;
		super.destroy();
	}
}
