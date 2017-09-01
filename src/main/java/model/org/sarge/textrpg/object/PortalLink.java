package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StringUtil;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.world.ExtendedLink;
import org.sarge.textrpg.world.Route;

/**
 * Link controlled by a {@link Portal}.
 * @author Sarge
 */
public class PortalLink extends ExtendedLink {
	private final Portal portal;

	/**
	 * Constructor.
	 * @param route		Route-type
	 * @param script	Script
	 * @param size		Size constraint
	 * @param portal	Controlling object
	 */
	public PortalLink(Route route, Script script, Size size, Portal portal) {
		super(route, script, size);
		Check.notNull(portal);
		this.portal = portal;
	}

	@Override
	public Optional<Thing> getController() {
		return Optional.of(portal);
	}

	/**
	 * @return Whether this link is open
	 */
	public boolean isOpen() {
		return portal.getOpenableModel().map(Openable::isOpen).orElse(true);
	}

	@Override
	public String reason(Actor actor) {
		if(isOpen()) {
			return super.reason(actor);
		}
		else {
			return "move.link.closed";
		}
	}

	@Override
	public String describe(String dir) {
		if(portal.getOpenableModel().isPresent()) {
			if(isOpen()) {
				return super.describe(StringUtil.wrap(dir, "(", ")"));
			}
			else {
				return super.describe(StringUtil.wrap(dir, "[", "]"));
			}
		}
		else {
			return super.describe(dir);
		}
	}

	@Override
	public Description.Builder describe() {
		if(isOpen()) {
			return super.describe();
		}
		else {
			final Description.Builder builder = new Description.Builder("exit.closed");
			final Cardinality cardinality = portal.getDescriptor().getCharacteristics().getCardinality();
			cardinality.add(builder);
			builder.wrap("name", portal.getName());
			return builder;
		}
	}
}
