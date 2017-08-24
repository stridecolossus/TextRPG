package org.sarge.textrpg.object;

import java.util.function.Predicate;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.Location;

/**
 * Action to fill a receptacle or lantern.
 * @author Sarge
 */
public class FillAction extends AbstractAction {
	/**
	 * Matcher for water receptacles.
	 */
	private static final Predicate<Thing> WATER = t -> {
		if(t instanceof Receptacle) {
			final Receptacle rec = (Receptacle) t;
			return rec.getDescriptor().getLiquid() == Liquid.WATER;
		}
		else {
			return false;
		}
	};

	/**
	 * Matcher for oil receptacles.
	 */
	private static final Predicate<WorldObject> OIL = ContentsHelper.receptacleMatcher(Liquid.OIL);

	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Fills a receptacle.
	 * @param actor
	 * @param rec
	 * @throws ActionException
	 */
	public ActionResponse fill(Entity actor, Receptacle rec) throws ActionException {
		// Find source
		final Receptacle src;
		final Location loc = actor.getLocation();
		if(loc.isWaterAvailable()) {
			// Fill from global source
			src = Receptacle.WATER;
		}
		else {
			// Otherwise look for a water source in the current location
			src = (Receptacle) loc.getContents().stream().filter(WATER).findFirst().orElseThrow(() -> new ActionException("fill.requires.water"));
		}

		// Fill from source
		rec.fill(src);

		// Build response
		return response(rec);
	}

	/**
	 * Fills a lantern.
	 * @param ctx
	 * @param actor
	 * @param light
	 * @throws ActionException
	 */
	public ActionResponse fill(Entity actor, Light light) throws ActionException {
		final Receptacle rec = (Receptacle) find(actor, OIL, false, "oil");
		light.fill(rec);
		// TODO - notify if receptacle is empty
		return response(light);
	}
}
