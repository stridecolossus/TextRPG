package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.stream.Stream;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.AbstractActiveAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.ActionHelper;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.MovementController;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

/**
 * Leave a {@link Vehicle} or a location with a single exit.
 * @author Sarge
 */
public class LeaveAction extends AbstractActiveAction {
	private final MovementController mover;

	/**
	 * Constructor.
	 * @param mover Movement controller
	 */
	public LeaveAction(MovementController mover) {
		this.mover = notNull(mover);
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}

	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Leave vehicle or location.
	 * @param ctx		Context
	 * @param actor		Actor
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse leave(Entity actor) throws ActionException {
		final Vehicle vehicle = ActionHelper.getVehicle(actor);
		final Location loc = actor.location();
		if(vehicle == null) {
			// Find single direction to follow
			final Stream<Direction> str = loc.getExits().entrySet().stream()
				.filter(e -> e.getValue().getLink().isTraversable(actor))
				.filter(e -> e.getValue().perceivedBy(actor))
				.map(e -> e.getKey());
			final Direction dir = StreamUtil.findOnly(str).orElseThrow(() -> new ActionException("leave.invalid.location"));

			// Leave and display destination
			final Description description = mover.move(actor, dir, 1, true);
			return new ActionResponse(description);
		}
		else {
			// Check swimming location
			if(!actor.isSwimming() && (loc.getTerrain() == Terrain.WATER)) throw new ActionException("move.not.swimming");

			// Leave vehicle
			actor.setParent(vehicle.parent());
			return response(vehicle);
		}
	}
}
