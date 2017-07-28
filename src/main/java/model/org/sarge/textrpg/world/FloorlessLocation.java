package org.sarge.textrpg.world;

import org.sarge.lib.util.Check;

/**
 * Location without a floor.
 * @author Sarge
 */
public class FloorlessLocation extends Location {
	private final Location base;

	/**
	 * Constructor.
	 * @param loc		Location
	 * @param base		Underneath location
	 */
	public FloorlessLocation(Location loc, Location base) {
		super(loc);
		Check.notNull(base);
		this.base = base;
	}
	
	@Override
	public Location getBase() {
		return base;
	}
}
