package org.sarge.textrpg.entity;

import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Direction;

/**
 * Entity movement notification.
 * @author Sarge
 */
public final class MovementNotification extends Notification {
	private final Direction dir;
	private final boolean arrival;

	/**
	 * Constructor.
	 * @param actor			Actor
	 * @param dir			Movement direction or <tt>null</tt> if none
	 * @param arrival		Whether arrival or departure event
	 */
	public MovementNotification(Entity actor, Direction dir, boolean arrival) {
		super(TextHelper.join("notification.movement", key(dir, arrival)), actor);
		this.dir = dir;
		this.arrival = arrival;
	}

	/**
	 * Determines the description key.
	 */
	private static String key(Direction dir, boolean arrival) {
		if(dir == null) {
			return arrival ? "appeared" : "disappeared";
		}
		else {
			return arrival ? "arrived" : "departed";
		}
	}

	/**
	 * @return Whether this is an arrival or departure notification
	 */
	public boolean isArrival() {
		return arrival;
	}

	@Override
	public void handle(Handler handler, Entity entity) {
		handler.handle(this, entity);
	}

	@Override
	protected void describe(Builder builder) {
		if(dir != null) {
			builder.add("dir", dir);
		}
	}
}
