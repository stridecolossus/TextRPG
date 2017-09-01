package org.sarge.textrpg.object;

import java.util.Collections;

import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;

/**
 * Boat or raft.
 * @author Sarge
 */
public class Boat extends Vehicle {
	/**
	 * Boat descriptor.
	 */
	public static class Descriptor extends Vehicle.Descriptor {
		private final boolean raft;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param mod				Movement cost modifier
		 * @param raft				Whether this is a raft or a boat
		 */
		public Descriptor(ContentsObjectDescriptor descriptor, float mod, boolean raft) {
			super(descriptor, Collections.emptySet(), mod);
			this.raft = raft;
		}

		@Override
		public boolean isValid(Route route) {
			return true;
		}

		/**
		 * @return Whether this vehicle is a raft or a boat
		 */
		public boolean isRaft() {
			return raft;
		}

		@Override
		public Boat create() {
			return new Boat(this);
		}
	}


	private boolean moored = true;

	/**
	 * Constructor.
	 * @param descriptor Vehicle descriptor
	 */
	public Boat(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	protected String getFullDescriptionKey() {
		if(moored) {
			return "boat.moored";
		}
		else {
			return super.getFullDescriptionKey();
		}
	}

	/**
	 * @return Whether this boat is moored
	 */
	public boolean isMoored() {
		return moored;
	}

	@Override
	public void setParent(Parent parent) throws ActionException {
		// Moor boat if moved into non-water location
		final Location loc = (Location) parent;
		moored = loc.getTerrain() != Terrain.WATER;

		// Delegate
		super.setParent(parent);
	}
}
