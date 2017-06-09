package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;

/**
 * Interact with a piece of {@link Furniture}.
 * @author Sarge
 */
public class FurnitureAction extends AbstractAction {
	private final Stance stance;
	
	/**
	 * Constructor.
	 * @param action Furniture action
	 */
	public FurnitureAction(Stance stance) {
		super("furniture." + stance.name());
		switch(stance) {
		case DEFAULT:
		case RESTING:
		case SLEEPING:
			break;
			
		default:
			throw new IllegalArgumentException();
		}
		this.stance = stance;
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	@Override
	protected Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
	}
	
	@Override
	public boolean isParentBlockedAction() {
		return true;
	}
	
	/**
	 * Interacts with piece of furniture.
	 * @param ctx
	 * @param actor
	 * @param furniture
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, Furniture furniture) throws ActionException {
		// Check can use this furniture
		if(!furniture.getDescriptor().isValid(stance)) throw new ActionException("furniture.invalid.stance", "action.furniture." + stance);
		if((actor.getStance() == stance) && (actor.getParent() == furniture)) throw new ActionException("furniture.already");
		
		// Use furniture
		actor.setParent(furniture);
		actor.setStance(stance);
		
		// Build response
		return response(furniture);
	}
}
