package org.sarge.textrpg.world;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StringUtil;

/**
 * Link with a specified {@link Route}.
 * @author Sarge
 */
public class RouteLink extends Link {
	private final Route route;

	/**
	 * Constructor.
	 * @param route Route-type
	 */
	public RouteLink(Route route) {
		Check.notNull(route);
		this.route = route;
	}

	@Override
	public Route route() {
		return route;
	}

	@Override
	public String describe(String dir) {
		return StringUtil.wrap(dir, route.getLeftIcon(), route.getRightIcon());
	}
}
