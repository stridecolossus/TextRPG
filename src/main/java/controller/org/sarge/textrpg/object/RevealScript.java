package org.sarge.textrpg.object;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.RevealNotification;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.util.Percentile;

/**
 * Script that reveals a {@link Hidden} object.
 * @author Sarge
 */
public class RevealScript implements Script {
	private final Thing thing;
	private final String key;

	/**
	 * Constructor.
	 * @param hidden Hidden object to reveal
	 */
	public RevealScript(Thing thing, String key) {
		Check.notNull(thing);
		Check.notEmpty(key);
		if(!thing.visibility().isLessThan(Percentile.ONE)) throw new IllegalArgumentException("Not a hidden object");
		this.thing = thing;
		this.key = key;
	}

	@Override
	public void execute(Actor actor) {
		if(!actor.perceives(thing)) {
			final Notification n = new RevealNotification(key, thing);
			actor.alert(n);
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
