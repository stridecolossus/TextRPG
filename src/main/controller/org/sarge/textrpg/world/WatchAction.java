package org.sarge.textrpg.world;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to watch at a vantage-point.
 * @author Sarge
 * @see Property#VANTAGE_POINT
 */
@Component
public class WatchAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public WatchAction() {
		super(Flag.OUTSIDE, Flag.INDUCTION);
	}

	@Override
	protected boolean isValid(Stance stance) {
		if(stance == Stance.MOUNTED) {
			return true;
		}
		else {
			return super.isValid(stance);
		}
	}

	/**
	 * Watches at this vantage-point.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the current location is not a vantage-point
	 */
	@RequiresActor
	public Response watch(Entity actor) throws ActionException {
		// Check in a vantage-point
		final var loc = actor.location();
		if(!loc.isProperty(Property.VANTAGE_POINT)) throw ActionException.of("watch.invalid.location");

		// Create watch callback
		final Runnable watcher = () -> {
			/**
			 *
			 * - enumerate locations within area / range using LocationVisitor
			 * - exclude dark terrain
			 * - cached?
			 * - add trigger for each
			 * - callback ->
			 * 		- perception check
			 * 		- modified by terrain, distance, weather
			 * 		- can only see lights if dark
			 * 		- alert actor
			 * - => this is empty
			 *
			 */
		};

		// Build response
		// TODO
		Runnable stop = () -> {};
		return Response.of(Induction.Instance.indefinite(stop));
	}
}
