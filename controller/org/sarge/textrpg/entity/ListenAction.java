package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.object.WorldObject;

/**
 * Listens action.
 * @author Sarge
 */
public class ListenAction extends AbstractAction {
	private static final String NOTHING = "listen.nothing";
	
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	@Override
	public boolean isParentBlockedAction() {
		return false;
	}
	
	@Override
	public ActionResponse execute(ActionContext ctx, Entity actor) throws ActionException {
		// TODO
		// - check for sounds in this location
		// - neighbours?
		// - how to know whether sounds already perceived?
		return null;
	}
	
	public void execute(ActionContext ctx, Entity actor, WorldObject obj) throws ActionException {
		final int score = actor.getAttributes().get(Attribute.PERCEPTION) * 10;
		final String sound = obj.getEmission(Emission.Type.SOUND).map(Emission::toString).orElse("none");
		System.out.println("listen "+sound); // TODO
		// TODO - message is no sounds
	}
	
	public void execute(ActionContext ctx, Entity actor, Entity entity) throws ActionException {
		// TODO - listen to someone, e.g. speaker, musician, etc
	}
}
