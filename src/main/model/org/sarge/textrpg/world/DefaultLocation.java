package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

/**
 * A <i>default</li> location is a free-standing place in the world with custom exits.
 * <p>
 * Default locations are best suited for modelling complex or spread-out areas with many custom exits and points-of-interest such as buildings, towns, mazes, etc.
 * <p>
 * Notes:
 * <ul>
 * <li>Default locations <b>must</b> have at least one exit unless the location is explicitly identified as an orphan using {@link Builder#orphan()}</li>
 * <li>A default location has exactly <b>one</b> connector (the location itself)</li>
 * </ul>
 * @author Sarge
 */
public class DefaultLocation extends Location {
	private final Area area;

	/**
	 * Constructor.
	 * @param descriptor		Location descriptor
	 * @param area				Area
	 */
	public DefaultLocation(Descriptor descriptor, Area area) {
		super(descriptor);
		this.area = notNull(area);
	}

	@Override
	public Area area() {
		return area;
	}

	/**
	 * Builder for a default location.
	 */
	public static class Builder {
		private Descriptor descriptor;
		private Area area = Area.ROOT;
		private boolean orphan = false;

		private DefaultLocation loc;

		/**
		 * Sets the location descriptor.
		 * @param descriptor Location descriptor
		 */
		public Builder descriptor(Descriptor descriptor) {
			this.descriptor = descriptor;
			return this;
		}

		/**
		 * Sets the area of this location.
		 * @param area Parent area
		 */
		public Builder area(Area area) {
			this.area = area;
			return this;
		}

	    /**
	     * Specifies that this location explicitly has no exits, e.g. for intermediate stops on a ferry.
	     */
		public Builder orphan() {
			orphan = true;
			return this;
		}

		/**
		 * Builds this location.
		 * @return Location
		 */
		public DefaultLocation build() {
			// Construct location
			if(loc != null) throw new IllegalStateException("Already built: " + loc);
			loc = new DefaultLocation(descriptor, area);

			// Init orphans
			if(orphan) {
				loc.exits = ExitMap.EMPTY;
			}

			return loc;
		}
	}
}
