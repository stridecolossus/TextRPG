package org.sarge.textrpg.entity;

import java.util.Collections;
import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
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

	/**
	 * Constructor.
	 * @param dir			Initial direction
	 * @param period		Iteration period (ms)
	 */
	public FollowRouteAction(Direction dir, long period) {
		super("follow." + dir.getMnenomic());
		Check.oneOrMore(period);
		this.dir = dir;
		this.period = period;
	}
	
	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING};
	}

	/**
	 * Follow a route.
	 * @param ctx
	 * @param actor
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse follow(ActionContext ctx, Entity actor) throws ActionException {
		// Check direction
		final Exit exit = actor.getLocation().getExits().get(dir);
		if(exit == null) throw new ActionException("follow.no.link");
		
		// Check available route
		final Route route = exit.getLink().getRoute();
		if(route == Route.NONE) throw new ActionException("follow.no.route");
		
		// Start following route
		final Predicate<Exit> filter = SelectFollower.route(Collections.singleton(route));
		final Follower follower = new SelectFollower(filter, Policy.ONE);
		final Induction induction = new FollowInduction(ctx, actor, follower, 1, "follow.route.finished");
		return new ActionResponse("follow.route.start", induction, period);
	}
}