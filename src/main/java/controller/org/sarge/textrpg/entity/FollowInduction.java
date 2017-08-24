package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.world.Direction;

/**
 * Induction for an auto-follow action.
 * @author Sarge
 */
public class FollowInduction implements Induction {
	private final Entity actor;
	private final Follower follower;
	private final MovementController mover;
	private final int mod;
	private final String message;

	/**
	 * Constructor.
	 * @param actor			Actor
	 * @param follower		Follower
	 * @param mover			Movement controller
	 * @param mod			Movement cost modifier
	 * @param message		Notification message when stopped following
	 */
	public FollowInduction(Entity actor, Follower follower, MovementController mover, int mod, String message) {
		Check.notNull(actor);
		Check.notNull(follower);
		Check.oneOrMore(mod);
		Check.notEmpty(message);
		this.actor = actor;
		this.follower = follower;
		this.mover = notNull(mover);
		this.mod = mod;
		this.message = message;
	}

	@Override
	public Description complete() throws ActionException {
		// Determine next direction to follow
		final Direction dir = follower.next(actor);
		
		// Follow
		final Description desc;
		if(dir == null) {
			// Stop if reached end of route
			actor.interrupt();
			return new Description(message);
		}
		else {
			// Traverse link
			desc = mover.move(actor, dir, mod, true);
		}
		
		return desc;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
