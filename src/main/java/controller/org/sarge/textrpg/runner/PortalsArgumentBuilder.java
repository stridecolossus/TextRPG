package org.sarge.textrpg.runner;

import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.Location;

/**
 * Argument builder for portals in the current location.
 * @author Sarge
 */
public class PortalsArgumentBuilder implements ArgumentBuilder {
	private final Location loc;
	
	/**
	 * Constructor.
	 * @param loc Location
	 */
	public PortalsArgumentBuilder(Location loc) {
		Check.notNull(loc);
		this.loc = loc;
	}

	@Override
	public Stream<Object> stream(Actor actor) {
		return loc.getExits().values().stream()
			.map(Exit::getLink)
			.map(Link::controller)
			.filter(Optional::isPresent)
			.map(Optional::get);
	}
}
