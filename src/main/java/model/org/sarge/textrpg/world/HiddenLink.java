package org.sarge.textrpg.world;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.util.Percentile;

/**
 * Partially visible link.
 * @author Sarge
 */
public class HiddenLink extends ExtendedLink {
	private final Thing controller;

	/**
	 * Constructor.
	 * @param route		Route-type
	 * @param script	Script
	 * @param name		Link name
	 * @param vis		Default visibility
	 * @param forget	Forget duration (ms)
	 */
	public HiddenLink(Route route, Script script, Size size, String name, Percentile vis, long forget) {
		super(route, script, size);
		Check.notNull(vis);
		this.controller = Thing.create(name, vis, false, forget);
	}
	
	@Override
	public Optional<Thing> getController() {
		return Optional.of(controller);
	}
}
