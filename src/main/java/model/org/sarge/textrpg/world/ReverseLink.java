package org.sarge.textrpg.world;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description.Builder;
import org.sarge.textrpg.common.Thing;

/**
 * Reverse link.
 * @author Sarge
 */
public class ReverseLink extends Link {
	private final Link link;

	/**
	 * Constructor.
	 * @param link Link
	 */
	public ReverseLink(Link link) {
		Check.notNull(link);
		this.link = link;
	}

	/**
	 * @return Reverse link
	 */
	public Link getLink() {
		return link;
	}

	@Override
	public Route getRoute() {
		return link.getRoute();
	}
	
	@Override
	public boolean isVisible(Actor actor) {
		return link.isVisible(actor);
	}
	
	@Override
	public boolean isTraversable(Actor actor) {
		return link.isTraversable(actor);
	}
	
	@Override
	public String getReason() {
		return link.getReason();
	}
	
	@Override
	public Optional<Thing> getController() {
		return link.getController();
	}
	
	@Override
	public String describe(String dir) {
		return link.describe(dir);
	}
	
	@Override
	public Builder describe() {
		return link.describe();
	}
}
