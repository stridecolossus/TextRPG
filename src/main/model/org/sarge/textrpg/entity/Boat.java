package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.entity.Vehicle.AbstractVehicle;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Trail;

/**
 * Boat or raft that can be used to traverse water locations.
 * @author Sarge
 */
public class Boat extends AbstractVehicle {
	/**
	 * Boat descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final LimitsMap limits;
		private final boolean raft;

		/**
		 * Constructor.
		 * @param descriptor		Vehicle descriptor
		 * @param limits			Contents limits
		 * @param raft				Whether this is a boat or a raft
		 */
		public Descriptor(ObjectDescriptor descriptor, LimitsMap limits, boolean raft) {
			super(descriptor);
			this.limits = notNull(limits);
			this.raft = raft;
		}

		@Override
		public Boat create() {
			return new Boat(this);
		}

		@Override
		public boolean isFixture() {
			return true;
		}
	}

	private boolean moored;

	/**
	 * Constructor.
	 * @param descriptor Vehicle descriptor
	 */
	public Boat(Descriptor descriptor) {
		super(descriptor, descriptor.limits, descriptor.raft ? "on" : Contents.PLACEMENT_DEFAULT);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	// TODO - frozen water, moored -> false if not frozen, not valid if frozen

	/**
	 * @return Whether this boat is moored up
	 */
	protected boolean isMoored() {
		return moored;
	}

	@Override
	public boolean isRaft() {
		return descriptor().raft;
	}

	/**
	 * Determines whether the given destination is water or frozen.
	 * @param dest Destination
	 * @return Whether water destination
	 */
	private static boolean isWater(Location dest) {
		return dest.isWater() && !dest.isFrozen();
	}

	@Override
	public boolean isValid(Exit exit) {
		if(moored && !isWater(exit.destination())) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	protected Trail trail() {
		return null;
	}

	@Override
	public Percentile tracks() {
		return Percentile.ZERO;
	}

	@Override
	public Percentile noise() {
		return Percentile.ZERO;
	}

	@Override
	protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
		if(moored) {
			builder.add(KEY_STATE, "boat.moored");
		}
		super.describe(carried, builder, formatters);
	}

	@Override
	public void move(Location dest) {
		super.move(dest);
		moored = !isWater(dest);
	}
}
