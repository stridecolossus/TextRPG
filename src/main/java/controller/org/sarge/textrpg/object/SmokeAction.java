package org.sarge.textrpg.object;

import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Induction;

/**
 * Smoke weed action.
 * @author Sarge
 */
public class SmokeAction extends AbstractAction {
	private static final Predicate<WorldObject> PIPE = ContentsHelper.categoryMatcher("pipe");
	private static final Predicate<WorldObject> WEED = ContentsHelper.categoryMatcher("weed");
	
	private final long duration;

	/**
	 * Constructor.
	 * @param duration Smoke duration
	 */
	public SmokeAction(long duration) {
		Check.oneOrMore(duration);
		this.duration = duration;
	}

	@Override
	public boolean isLightRequiredAction() {
		return false;
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
	 * Smoke any pipe-weed.
	 */
	@Override
	public ActionResponse execute(Entity actor) throws ActionException {
		return smoke(actor, WEED);
	}
	
	/**
	 * Smoke specified pipe-weed.
	 */
	public ActionResponse execute(Entity actor, String weed) throws ActionException {
		return smoke(actor, WEED.and(obj -> obj.name().equals(weed)));
	}

	/**
	 * Smoke.
	 * @param ctx			Context
	 * @param actor			Actor
	 * @param matcher		Pipe-weed matcher
	 * @throws ActionException if the actor does not possess a pipe or matched pipe-weed.
	 */
	private ActionResponse smoke(Entity actor, Predicate<WorldObject> matcher) throws ActionException {
		// Check for pipe
		find(actor, PIPE, true, "pipe");
		
		// Find weed
		final WorldObject weed = find(actor, matcher, true, "weed");
		
		// Start smoking
		final Induction induction = () -> {
			// TODO - effects?
			weed.destroy();
			return new Description("smoke.response", "weed", weed);
		};
		return new ActionResponse("smoke.start", induction, duration);
	}
}
