package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.Vehicle;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;

/**
 * Moves the actor in a given direction.
 * @author Sarge
 * @see ActionHelper#move(ActionContext, Entity, Direction, int)
 */
public class MoveAction extends AbstractAction {
	private final Direction dir;
	private final MovementController mover;
	
	/**
	 * Constructor.
	 * @param dir Movement direction
	 */
	public MoveAction(Direction dir, MovementController mover) {
		super(dir.name());
		this.dir = dir;
		this.mover = notNull(mover);
	}
	
	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.SLEEPING};
	}
	
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}
	
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Move in direction.
	 */
	public ActionResponse move(Entity actor) throws ActionException {
		// Check can move
		final String name = actor.getParent().getParentName();
		switch(name) {
		case Location.NAME:
		case Vehicle.NAME:
			break;
			
		default:
			throw new ActionException("move.invalid." + name);
		}
		
		// Check valid link
		final Location loc = actor.getLocation();
		final Exit exit = loc.getExits().get(dir);
		if((exit == null) || !exit.perceivedBy(actor)) throw new ActionException("move.invalid.direction");

		// Move in specified direction
		final Description desc = mover.move(actor, dir, 1, true);
		
		// Display location
		return new ActionResponse(desc);
	}
}
