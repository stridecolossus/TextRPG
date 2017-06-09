package org.sarge.textrpg.entity;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.world.Direction;

/**
 * Induction for an auto-follow action.
 * @author Sarge
 */
public class FollowInduction implements Induction {
	private final ActionContext ctx;
	private final Entity actor;
	private final Follower follower;
	private final int mod;
	private final String message;

	/**
	 * Constructor.
	 * @param ctx			Context
	 * @param actor			Actor
	 * @param follower		Follower
	 * @param mod			Movement cost modifier
	 * @param message		Notification message when stopped following
	 */
	public FollowInduction(ActionContext ctx, Entity actor, Follower follower, int mod, String message) {
		Check.notNull(ctx);
		Check.notNull(actor);
		Check.notNull(follower);
		Check.oneOrMore(mod);
		Check.notEmpty(message);
		this.ctx = ctx;
		this.actor = actor;
		this.follower = follower;
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
			desc = ctx.getMovementController().move(ctx, actor, dir, mod, true);
		}
		
		return desc;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
