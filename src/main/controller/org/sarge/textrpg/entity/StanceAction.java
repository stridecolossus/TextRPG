package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.stereotype.Component;

/**
 * Action to change the current stance.
 * @author Sarge
 */
@Component
@RequiresActor
public class StanceAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public StanceAction() {
		super(Flag.ACTIVE);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Stand up.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor cannot stand
	 */
	public Response stand(Entity actor) throws ActionException {
		return stance(actor, Stance.DEFAULT);
	}

	/**
	 * Rest.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor cannot rest
	 */
	public Response rest(Entity actor) throws ActionException {
		return stance(actor, Stance.RESTING);
	}

	/**
	 * Go to sleep.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor cannot go to sleep
	 */
	public Response sleep(Entity actor) throws ActionException {
		return stance(actor, Stance.SLEEPING);
	}

	/**
	 * Go to sleep in the given bed-roll.
	 * @param actor 		Actor
	 * @param bedroll		Bed-roll
	 * @return Response
	 * @throws ActionException if the actor cannot go to sleep or the given object is not a bed-roll
	 */
	public Response sleep(Entity actor, @Carried WorldObject bedroll) throws ActionException {
		if(!bedroll.isCategory("bedroll")) throw ActionException.of("sleep.invalid.bedroll");
		// TODO - apply transient warmth effect?
		return stance(actor, Stance.SLEEPING);
	}

	/**
	 * Wake up.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor is not asleep
	 */
	public Response wake(Entity actor) throws ActionException {
		if(actor.model().stance() != Stance.SLEEPING) throw ActionException.of("stance.not.alseep");
		return stance(actor, Stance.RESTING);
	}

	/**
	 * Changes the stance of the given actor.
	 * @param actor			Actor
	 * @param stance		New stance
	 * @return Response
	 * @throws ActionException if the stance is invalid
	 * @see Stance#isInductionBlocked(Stance)
	 */
	private static Response stance(Entity actor, Stance stance) throws ActionException {
		// Check stance transition
		final Stance current = actor.model().stance();
		if(current == stance) throw ActionException.of("stance.already", stance.name());

		// Update stance
		if(current.isVisibilityModifier()) {
			actor.model().values().visibility().remove();
		}
		actor.model().stance(stance);

		// Build response
		return Response.of(TextHelper.join("action.stance", stance.name()));
	}
}
