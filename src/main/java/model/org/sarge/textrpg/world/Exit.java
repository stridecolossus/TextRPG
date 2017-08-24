package org.sarge.textrpg.world;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Hidden;

/**
 * Exit from a location.
 */
public final class Exit {
	private final Location dest;
	private final Link link;

	/**
	 * Constructor.
	 * @param dest		Destination
	 * @param link		Link descriptor
	 */
	public Exit(Location dest, Link link) {
		Check.notNull(dest);
		Check.notNull(link);
		this.dest = dest;
		this.link = link;
	}

	/**
	 * @return Destination
	 */
	public Location getDestination() {
		return dest;
	}

	/**
	 * @return Link descriptor
	 */
	public Link getLink() {
		return link;
	}

	/**
	 * Helper.
	 * @param actor Actor
	 * @return Whether the given actor can perceive this exit
	 * @see #getController()
	 * @see Actor#perceives(Hidden)
	 */
	public final boolean perceivedBy(Actor actor) {
		return link.getController().map(actor::perceives).orElse(true);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
