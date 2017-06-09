package org.sarge.textrpg.world;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;

/**
 * Link with an associated script and size constraint.
 * @author Sarge
 */
public class ExtendedLink extends RouteLink {
	private final Script script;
	private final Size size;
	
	/**
	 * Constructor.
	 * @param route			Route-type
	 * @param script		Script
	 * @param size			Maximum size to traverse this link
	 */
	public ExtendedLink(Route route, Script script, Size size) {
		super(route);
		Check.notNull(script);
		Check.notNull(size);
		this.script = script;
		this.size = size;
	}
	
	@Override
	public Size getSize() {
		return size;
	}
	
	@Override
	public Script getScript() {
		return script;
	}
}
