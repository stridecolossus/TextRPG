package org.sarge.textrpg.world;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;

/**
 * Link to a fake destination that cannot be reached.
 * @author Sarge
 */
public class FakeLink extends ExtendedLink {
	private final String name;
	private final String message;

	/**
	 * Constructor.
	 * @param route			Route
	 * @param size			Size constraint
	 * @param name			Destination name
	 * @param message		Message to display
	 */
	public FakeLink(Route route, Size size, String name, String message) {
		super(route, Script.NONE, size);
		Check.notEmpty(name);
		Check.notEmpty(message);
		this.name = name;
		this.message = message;
	}

	@Override
	public String reason(Actor actor) {
		return message;
	}

	@Override
	public String getDestinationName(Location dest) {
		return name;
	}
}
