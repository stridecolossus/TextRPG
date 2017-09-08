package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.world.Direction;

/**
 * Entity-manager action that moves the entity using a {@link Follower}.
 * @author Sarge
 */
public class MoveEntityAction implements EntityManager.Action {
	private static final Logger LOG = Logger.getLogger(MovementController.class.getName());

	private final Follower follower;
	private final MovementController mover;
	private final boolean stop;
	
	/**
	 * Constructor.
	 * @param follower		Follower
	 * @param mover			Movement controller
	 * @param stop			Whether to stop when the follower has finished
	 */
	public MoveEntityAction(Follower follower, MovementController mover, boolean stop) {
		Check.notNull(follower);
		this.follower = follower;
		this.mover = notNull(mover);
		this.stop = stop;
	}

	@Override
	public boolean execute(Entity entity) {
		// Select next direction
		final Direction dir = follower.next(entity);
		if(dir == null) {
			if(!stop) {
				LOG.log(Level.WARNING, String.format("Cannot move entity: entity=%s loc=%s", entity, entity.location()));
			}
			return true;
		}
		
		// Move entity
		try {
			mover.move(entity, dir, 1, false);
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
