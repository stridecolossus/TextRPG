package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.Weapon.Crossbow;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to un/load a crossbow.
 * @author Sarge
 */
@Component
@RequiresActor
public class LoadAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public LoadAction() {
		super(Flag.OUTSIDE, Flag.BROADCAST);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	protected boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Loads the equipped crossbow with the next available bolt.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor does not have an equipped crossbow or an available bolt
	 */
	public Response load(Entity actor) throws ActionException {
//		final Quiver quiver = actor.contents().equipment().quiver(Ammo.Type.BOLT).orElseThrow(() -> ActionException.of("load.requires.quiver"));
//		final Ammo bolt = quiver.next(Ammo.Type.BOLT).orElseThrow(() -> ActionException.of("load.requires.bolt"));
//		return load(actor, bolt);
		// TODO
		return null;
	}

	/**
	 * Loads the equipped crossbow with the given bolt.
	 * @param actor		Actor
	 * @param bolt		Bolt to equip
	 * @return Response
	 * @throws ActionException if the actor does not have an equipped crossbow or the given ammo is not a bolt
	 */
	public Response load(Entity actor, @Carried Ammo bolt) throws ActionException {
		final Crossbow crossbow = find(actor);
		crossbow.load(bolt);
		return Response.OK;
	}

	/**
	 * Unloads the bolt in the equipped crossbow.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor does not have an equipped crossbow or is is not loaded
	 */
	public Response unload(Entity actor) throws ActionException {
		final Crossbow crossbow = find(actor);
		crossbow.unload();
		return Response.OK;
	}

	/**
	 * Finds an equipped crossbow.
	 */
	private static Crossbow find(Entity actor) throws ActionException {
		return actor.contents().equipment().weapon()
			.filter(obj -> obj instanceof Crossbow)
			.map(Crossbow.class::cast)
			.orElseThrow(() -> ActionException.of("load.requires.crossbow"));
	}
}
