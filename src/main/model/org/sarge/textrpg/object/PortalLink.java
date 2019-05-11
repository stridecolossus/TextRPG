package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.object.Portal.PortalState;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.ExtendedLink;

/**
 * A portal-link has a {@link Portal} controller.
 * @author Sarge
 */
public class PortalLink extends ExtendedLink {
	/**
	 * Wraps a direction name for a portal.
	 * @param dir		Direction
	 * @param open		Whether open
	 * @return Wrapped name
	 */
	public static String wrap(String dir, boolean open) {
		if(open) {
			return StringUtils.join("(", dir, ")");
		}
		else {
			return StringUtils.join("[", dir, "]");
		}
	}

	private final Portal portal;

	/**
	 * Constructor.
	 * @param props			Properties
	 * @param portal		Portal
	 */
	public PortalLink(ExtendedLink.Properties props, Portal portal) {
		super(props);
		this.portal = notNull(portal);
	}

	@Override
	public Optional<Thing> controller() {
		return Optional.of(portal);
	}

	@Override
	public boolean isQuiet() {
		return portal.isQuiet();
	}

	@Override
	public boolean isTraversable() {
		switch(portal.state()) {
		case BLOCKED:		return false;
		case BROKEN:		return true;
		default:			return portal.model().isOpen();
		}
	}

	@Override
	public Optional<Description> reason(Thing actor) {
		final PortalState state = portal.state();
		if(state == PortalState.BLOCKED) {
			return Optional.of(new Description("portal.blocked", portal.name()));
		}
		else
		if(portal.model().isOpen() || (state == PortalState.BROKEN)) {
			return super.reason(actor);
		}
		else {
			return Optional.of(new Description("portal.closed", portal.name()));
		}
	}

	@Override
	public String key() {
		final PortalState state = portal.state();
		switch(state) {
		case BLOCKED:
		case BROKEN:
			return state.name();

		default:
			if(portal.model().isOpen()) {
				return super.key();
			}
			else {
				return "closed";
			}
		}
	}

	@Override
	public String wrap(String dir) {
		switch(portal.state()) {
		case BLOCKED:		return StringUtils.wrap(dir, "!");
		case BROKEN:		return StringUtils.wrap(dir, "#");
		default:			return wrap(dir, portal.model().isOpen());
		}
	}
}
