package org.sarge.textrpg.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;

/**
 * Action to list the exits from the current location.
 * @author Sarge
 */
public class ListExitsAction extends AbstractAction {
	public ListExitsAction() {
		super("exits");
	}
	
	@Override
	public boolean isCombatBlockedAction() {
		return false;
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
	 * List exits.
	 * @param ctx
	 * @param actor
	 * @throws ActionException
	 */
	public ActionResponse listExits(ActionContext ctx, Entity actor) throws ActionException {
		// Build description for each available link
		final Location loc = actor.getLocation();
		final boolean daylight = ctx.isDaylight();
		final boolean light = loc.isLightAvailable(daylight);
		final Map<Direction, Exit> links = actor.getLocation().getExits();
		final List<Description> exits = new ArrayList<>();
		for(Direction dir : Direction.values()) {
			// Lookup link
			final Exit exit = links.get(dir);
			if(exit == null) continue;
			
			// Check is visible to this actor
			final Link link = exit.getLink();
			if(!link.isVisible(actor)) continue;

			// Build description
			final Description.Builder builder = link.describe();
			final String name = link.getDestinationName(exit.getDestination());
			builder.add("dir", Location.describe(dir.name().toLowerCase(), exit));
			builder.wrap("dest", light ? name : "list.exits.unknown");
			
			// Add exit
			exits.add(builder.build());
		}

		// Build response
		return new ActionResponse(Description.create("list.exits", exits));
	}
}
