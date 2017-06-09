package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Openable.Operation;
import org.sarge.textrpg.entity.ActionHelper;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.world.Location;

/**
 * Action to use a portal or other openable objects.
 * @author Sarge
 * @see WorldObject#getOpenableModel()
 */
public class PortalAction extends AbstractAction {
	private final Operation op;

	/**
	 * Constructor.
	 * @param op Operation
	 */
	public PortalAction(Operation op) {
		super(op.name());
		Check.notNull(op);
		this.op = op;
	}

	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
	}

	@Override
	public boolean isCombatBlockedAction() {
		return false;
	}
	
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}
	
	/**
	 * Manipulates a portal.
	 * @param ctx
	 * @param actor
	 * @param portal
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, Portal portal) throws ActionException {
		return execute(ctx, actor, portal, (Location) portal.getDestination());
	}

	/**
	 * Manipulates something that is {@link Openable}.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, WorldObject obj) throws ActionException {
		obj.getOpenableModel().orElseThrow(() -> new ActionException("portal.not.openable", op));
		return execute(ctx, actor, obj, null);
	}

	/**
	 * Opens a hidden link.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Player player, Hidden obj) throws ActionException {
		if(op != Operation.OPEN) return INVALID;
		if(player.perceives(obj)) throw new ActionException("openable.already", op);
		player.add(obj);
		return build(op, obj.toString());
	}
	
	/**
	 * Applies action.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @param other
	 * @return
	 * @throws ActionException
	 */
	private ActionResponse execute(ActionContext ctx, Entity actor, WorldObject obj, Location dest) throws ActionException {
		// Check for required key
		final Openable model = obj.getOpenableModel().get();
		if(op.isLocking() && model.isLockable()) {
			final String key = model.getLock().getKey();
			if(!actor.getContents().stream().anyMatch(t -> t.getName().equals(key))) {
				throw new ActionException("portal.requires.key");
			}
		}
		
		// Apply action to openable
		model.apply(op);
		
		// Register reset event
		ActionHelper.registerOpenableEvent(Portal.QUEUE, actor.getLocation(), dest, obj, "portal.auto.close");
		
		// Build response
		return build(op, obj.getName());
	}
	
	private static ActionResponse build(Operation op, String name) {
		final Description desc = new Description.Builder("portal.response")
			.wrap("op", op.name())
			.wrap("name", name)
			.build();
		return new ActionResponse(desc);
	}
}
