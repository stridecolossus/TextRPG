package org.sarge.textrpg.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.world.Direction;

/**
 * Entity-manager action that moves the entity using a {@link Follower}.
 * @author Sarge
 */
public class MoveEntityAction implements EntityManager.Action {
	private static final Logger LOG = Logger.getLogger(MovementController.class.getName());

	private final ActionContext ctx;
	private final Follower follower;
	private final boolean stop;
	
	/**
	 * Constructor.
	 * @param ctx			Context
	 * @param follower		Follower
	 * @param stop			Whether to stop when the follower has finished
	 */
	public MoveEntityAction(ActionContext ctx, Follower follower, boolean stop) {
		Check.notNull(ctx);
		Check.notNull(follower);
		this.ctx = ctx;
		this.follower = follower;
		this.stop = stop;
	}

	@Override
	public boolean execute(Entity entity) {
		// Select next direction
		final Direction dir = follower.next(entity);
		if(dir == null) {
			if(!stop) {
				LOG.log(Level.WARNING, String.format("Cannot move entity: entity=%s loc=%s", entity, entity.getLocation()));
			}
			return true;
		}
		
		// Move entity
		try {
			ctx.getMovementController().move(ctx, entity, dir, 1, false);
		}
		catch(ActionException e) {
			LOG.log(Level.SEVERE, "Error moving entity: " + entity, e);
		}

		return false;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
