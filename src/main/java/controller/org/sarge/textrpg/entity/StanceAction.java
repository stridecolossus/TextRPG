package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.object.Furniture;
import org.sarge.textrpg.object.Vehicle;

/**
 * Action to change {@link Stance}.
 * @author Sarge
 */
public class StanceAction extends AbstractAction {
	private final Stance stance;

	/**
	 * Constructor.
	 * @param stance Stance
	 * @throws IllegalArgumentException for stances that are controlled by other actions, e.g. {@link Stance#SNEAKING}
	 */
	public StanceAction(Stance stance) {
		super(stance.name());
		switch(stance) {
		case COMBAT:
		case MOUNTED:
		case SNEAKING:
			throw new IllegalArgumentException("Invalid stance action: " + stance);
		}
		this.stance = stance;
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	@Override
	public boolean isValidStance(Stance stance) {
		return true;
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
	 * Changes stance.
	 * @param ctx
	 * @param actor
	 * @throws ActionException
	 */
	@Override
	public ActionResponse execute(ActionContext ctx, Entity actor) throws ActionException {
		switch(actor.getParent().getParentName()) {
		case Vehicle.NAME:
			if(stance == Stance.DEFAULT) {
				// Leave vehicle
				final Parent parent = actor.getParent();
				actor.setParent(actor.getLocation());
				actor.setStance(Stance.DEFAULT);
				return new ActionResponse(new Description("action.leave", "name", parent));
			}
			else {
				return new ActionResponse("action.invalid.furniture");
			}
			
		case Furniture.NAME:
			if(stance == Stance.DEFAULT) {
				// Stop using furniture
				actor.setStance(Stance.DEFAULT);
				actor.setParent(actor.getLocation());
				return new ActionResponse("response.default");
			}
			else {
				// Otherwise change stance within the furniture
				final Furniture furniture = (Furniture) actor.getParent();
				if(furniture.getDescriptor().isValid(stance)) throw new ActionException("furniture.invalid.stance", stance);
				actor.setStance(stance);
				return new ActionResponse("response.furniture." + stance);
			}
			
		default:
			// Change stance
			final Stance current = actor.getStance();
			if(stance == current) throw new ActionException("stance.already", "stance." + stance);
			if(!stance.isValidTransition(current)) throw new ActionException("stance.invalid");
			actor.setStance(stance);
			return new ActionResponse("response." + stance);
		}
	}
}
