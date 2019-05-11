package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

/**
 * A <i>route-link</i> is a simple link with an associated {@link Route}.
 * @author Sarge
 * TODO - RestrictedLink extends RouteLink?
 */
public class RouteLink extends Link {
	private static final List<RouteLink> ROUTES = Arrays.stream(Route.values()).map(RouteLink::new).collect(toList());

	/**
	 * Creates a route-link for the given route.
	 * @param route Route
	 * @return Route-link
	 */
	public static RouteLink of(Route route) {
		if(route == Route.NONE) throw new IllegalArgumentException("Invalid route-link: " + route);
		return ROUTES.get(route.ordinal());
	}

	private final Route route;

	/**
	 * Constructor.
	 * @param route Route
	 */
	private RouteLink(Route route) {
		this.route = route;
	}

	@Override
	public Route route() {
		return route;
	}
}
