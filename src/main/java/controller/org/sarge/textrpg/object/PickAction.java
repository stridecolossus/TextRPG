package org.sarge.textrpg.object;

import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Openable.Operation;
import org.sarge.textrpg.common.Openable.State;
import org.sarge.textrpg.entity.ActionHelper;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Induction;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.world.Location;

/**
 * Pick-lock action.
 * @author Sarge
 */
public class PickAction extends AbstractAction {
	private final Skill pick;
	private final Predicate<WorldObject> matcher;
	private final long mod;

	/**
	 * Constructor.
	 * @param pick			Pick-lock skill descriptor
	 * @param lockpicks		Lock-picks descriptor
	 * @param mod			Duration multiplier
	 */
	public PickAction(Skill pick, ObjectDescriptor lockpicks, long mod) {
		Check.notNull(pick);
		Check.oneOrMore(mod);
		this.pick = pick;
		this.matcher = ContentsHelper.objectMatcher(lockpicks);
		this.mod = mod;
	}

	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
	}

	/**
	 * Picks an openable object.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @throws ActionException
	 */
	public ActionResponse pick(ActionContext ctx, Entity actor, WorldObject obj) throws ActionException {
		final Openable model = obj.getOpenableModel().orElseThrow(() -> new ActionException("pick.not.openable"));
		return pick(ctx, actor, obj, model, null);
	}

	/**
	 * Picks a portal.
	 * @param ctx
	 * @param actor
	 * @param portal
	 * @throws ActionException
	 */
	public ActionResponse pick(ActionContext ctx, Entity actor, Portal portal) throws ActionException {
		return pick(ctx, actor, portal, portal.getOpenableModel().get(), (Location) portal.getDestination());
	}

	/**
	 * Pick-lock.
	 * @param ctx
	 * @param actor
	 * @param obj		Object to pick
	 * @param model		Openable model
	 * @param dest		Destination for a portal
	 * @throws ActionException
	 */
	private ActionResponse pick(ActionContext ctx, Entity actor, WorldObject obj, Openable model, Location dest) throws ActionException {
		// Check can picked
		if(model.isLockable()) throw new ActionException("pick.not.lockable");
		if(model.getState() != State.LOCKED) throw new ActionException("pick.not.locked");

		// Check actor has pick skill
		final int score = getSkillLevel(actor, pick);

		// Check actor has lock-picks
		final WorldObject lockpicks = find(actor, matcher, false, "lockpicks");

		// Start lock-pick
		final Induction induction = () -> {
			// Apply wear
			lockpicks.wear();

			// Add experience
			// TODO

			// Check for impossible lock for this actor
			if(score < model.getLock().getPickDifficulty().intValue()) {
				throw new ActionException("pick.too.difficult");
			}

			// Pick lock
			model.apply(Operation.UNLOCK);
			ActionHelper.registerOpenableEvent(actor.getLocation(), dest, obj, "lock.auto.close");

			// Notify success
			return new Description("pick.success");
		};
		return new ActionResponse("pick.start", induction, score * mod);
	}
}
