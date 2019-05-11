package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.textrpg.util.TextHelper;

/**
 * River current link.
 * @author Sarge
 */
public class CurrentLink extends Link {
	/**
	 * Helper - Finds the current link in the given location.
	 * @param loc Location
	 * @return Current link
	 */
	public static Optional<Exit> find(Location loc) {
		return loc.exits().stream().filter(exit -> exit.link() instanceof CurrentLink).findAny();
	}

	/**
	 * Current type.
	 */
	public enum Current {
		SLOW,
		MEDIUM,
		FAST,
		RAPIDS,
		WATERFALL
	}

	private final Current current;

	/**
	 * Constructor.
	 * @param current Current type
	 */
	public CurrentLink(Current current) {
		this.current = notNull(current);
	}

	/**
	 * @return Current type
	 */
	public Current current() {
		return current;
	}

	@Override
	public Route route() {
		return Route.RIVER;
	}

	@Override
	public String wrap(String dir) {
		switch(current) {
		case RAPIDS:
		case WATERFALL:
			return TextHelper.wrap(dir, '!');

		default:
			return Route.RIVER.wrap(dir);
		}
	}
}
