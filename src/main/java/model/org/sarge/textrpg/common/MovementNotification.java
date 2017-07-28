package org.sarge.textrpg.common;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.Direction;

/**
 * Notifies the arrival or departure of an {@link Entity}.
 * @author Sarge
 */
public class MovementNotification extends AbstractNotification {
	private final Actor actor;
	private final boolean arrival;
	private final Optional<Direction> dir;
	private final Description desc;

	/**
	 * Constructor.
	 * @param actor			Actor that generated this notification
	 * @param arrival		Whether arriving or departing
	 * @param dir			Direction taken (optional)
	 */
	public MovementNotification(Actor actor, boolean arrival, Direction dir, boolean player) {
		Check.notNull(actor);
		this.actor = actor;
		this.arrival = arrival;
		this.dir = Optional.ofNullable(dir);
		this.desc = build(player);
	}
	
	private Description build(boolean player) {
		// Determine arrival key
		final String key;
		if(dir.isPresent()) {
			key = arrival ? "arrives" : "departs";
		}
		else {
			key = arrival ? "appears" : "disappears";
		}
		
		// Build description
		final Description.Builder builder = new Description.Builder("movement.notification");
		builder.wrap("arrival", "movement.notification." + key);
		dir.ifPresent(d -> {
			if(arrival) {
				builder.wrap("dir", "movement.notification." + d);
			}
			else {
				builder.wrap("dir", d);
			}
		});
		if(player) {
			builder.add("name", actor);
		}
		else {
			builder.wrap("name", actor);
		}
		return builder.build();
	}

	/**
	 * @return Actor
	 */
	public Actor getActor() {
		return actor;
	}

	/**
	 * @return Whether arriving or departing
	 */
	public boolean isArrival() {
		return arrival;
	}

	/**
	 * @return Arrival/departure direction
	 */
	public Optional<Direction> getDirection() {
		return dir;
	}
	
	@Override
	public void accept(Handler handler) {
		handler.handle(this);
	}
	
	@Override
	public Description describe() {
		return desc;
	}
}
