package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.util.BandingTable;

/**
 * An object that can be damaged and repaired.
 * @author Sarge
 */
public class DurableObject extends WorldObject {
	/**
	 * Descriptor for this object.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final int durability;

		/**
		 * Constructor.
		 * @param descriptor Object descriptor
		 * @param durability Durability (or number of uses)
		 */
		public Descriptor(ObjectDescriptor descriptor, int durability) {
			super(descriptor);
			Check.oneOrMore(durability);
			this.durability = durability;
		}

		@Override
		public String getDescriptionKey() {
			return "durable";
		}

		/**
		 * @return Maximum durability
		 */
		public int getDurability() {
			return durability;
		}

		@Override
		public DurableObject create() {
			return new DurableObject(this);
		}
	}

	/**
	 * Wear banding table.
	 * TODO - move this to descriptor
	 */
	private static final BandingTable TABLE = new BandingTable.Builder()
		.add(0, "broken")
		.add(25, "bad")
		.add(50, "damaged")
		.add(75, "slight")
		.add(100, "new")
		.build();

	/**
	 * Constructor.
	 * @param descriptor Descriptor
	 */
	public DurableObject(Descriptor descriptor) {
		super(descriptor);
	}

	private int wear;

	@Override
	protected void describe(Description.Builder builder) {
		final Descriptor descriptor = (Descriptor) super.getDescriptor();
		final float key = 100 * (descriptor.durability - wear) / (float) descriptor.durability;
		builder.wrap("wear", "wear." + TABLE.get(key));
	}

	/**
	 * @return Current wear
	 */
	protected int getWear() {
		return wear;
	}

	@Override
	public boolean isDamaged() {
		return wear > 0;
	}

	@Override
	public boolean isBroken() {
		final Descriptor descriptor = (Descriptor) super.getDescriptor();
		return wear >= descriptor.durability;
	}

	@Override
	public void wear() throws ActionException {
		if(isBroken()) throw new ActionException("durable.object.broken");
		++wear;
	}

	/**
	 * Repairs this durable object.
	 * @throws ActionException if this object is not damaged
	 */
	void repair() throws ActionException {
		if(wear == 0) throw new ActionException("repair.not.damaged");
		this.wear = 0;
	}
}
