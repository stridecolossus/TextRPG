package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.util.Randomiser;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Location;

/**
 * Flees the current location in a random direction.
 * @author Sarge
 */
public class FleeAction extends AbstractAction {
	private final int mod;

	/**
	 * Constructor.
	 * @param mod Movement cost modifier
	 */
	public FleeAction(int mod) {
		Check.oneOrMore(mod);
		this.mod = mod;
	}
	
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	@Override
	public boolean isCombatBlockedAction() {
		return false;
	}
	
	@Override
	public boolean isVisibleAction() {
		return true;
	}

	/**
	 * Flee from the current location.
	 * @param ctx
	 * @param actor
	 * @return
	 * @throws ActionException
	 */
	@Override
	public ActionResponse execute(ActionContext ctx, Entity actor) throws ActionException {
		// Enumerate available links
		final Location loc = actor.getLocation();
		final List<Direction> available = loc
			.getExits()
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue().getLink().isTraversable(actor))
			.filter(entry -> entry.getValue().perceivedBy(actor))
			.map(entry -> entry.getKey())
			.collect(toList());
		
		// Panic if no available links
		if(available.isEmpty()) {
			// TODO - inc panic
			throw new ActionException("flee.cannot.flee");
		}
		
		// Select random link
		final Direction dir = Randomiser.random(available);
		
		// Clear stance
		switch(actor.getStance()) {
		case COMBAT:
		case RESTING:
		case SNEAKING:
			actor.setStance(Stance.DEFAULT);
			break;
		}

		// Traverse link
		final Description desc = ctx.getMovementController().move(ctx, actor, dir, mod, true);
		
		// TODO - inc panic
		
		return new ActionResponse(Arrays.asList(new Description("flee.response"), desc));
	}
}