package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.Collections;
import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.SelectFollower;
import org.sarge.textrpg.world.SelectFollower.Policy;

/**
 * Action to follow a {@link Route}.
 * @author Sarge
 */
public class FollowRouteAction extends AbstractAction {
	private final Direction dir;
	private final long period;
	private final MovementController mover;

	/**
	 * Constructor.
	 * @param dir			Initial direction
	 * @param period		Iteration period (ms)
	 */
	public FollowRouteAction(Direction dir, long period, MovementController mover) {
		super("follow." + dir.getMnemonic());
		Check.oneOrMore(period);
		this.dir = dir;
		this.period = period;
		this.mover = notNull(mover);
	}

	@Override
	public boolean isValidStance(Stance stance) {
	    if(stance == Stance.RESTING) {
	        return false;
	    }
	    else {
	        return super.isValidStance(stance);
	    }
	}

	/**
	 * Follow a route.
	 * @param ctx
	 * @param actor
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse follow(Entity actor) throws ActionException {
		// Check direction
		final Exit exit = actor.location().getExits().get(dir);
		if(exit == null) throw new ActionException("follow.no.link");

		// Check available route
		final Route route = exit.getLink().route();
		if(route == Route.NONE) throw new ActionException("follow.no.route");

		// Start following route
		final Predicate<Exit> filter = SelectFollower.route(Collections.singleton(route));
		final Follower follower = new SelectFollower(filter, Policy.ONE);
		final Induction induction = new FollowInduction(actor, follower, mover, 1, "follow.route.finished");
		return new ActionResponse("follow.route.start", induction, period);
	}
}
