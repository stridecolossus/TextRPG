package org.sarge.textrpg.common;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.world.Direction;

/**
 * Notification for a nearby {@link Emission}.
 * @author Sarge
 */
public class EnvironmentNotification extends AbstractNotification {
	private final Emission emission;
	private final Direction dir;
	
	/**
	 * Constructor.
	 * @param emission		Emission descriptor
	 * @param dir			Approximate direction
	 */
	public EnvironmentNotification(Emission emission, Direction dir) {
		Check.notNull(emission);
		Check.notNull(dir);
		this.emission = emission;
		this.dir = dir;
	}
	
	public Emission getEmission() {
		return emission;
	}
	
	public Direction getDirection() {
		return dir;
	}

	@Override
	public Description describe() {
		return new Description.Builder("environment.notification")
			.add("type", emission.type())
			.add("intensity", emission.intensity())
			.add("dir", dir)
			.build();
	}

	@Override
	public void accept(Handler handler) {
		handler.handle(this);
	}
}
