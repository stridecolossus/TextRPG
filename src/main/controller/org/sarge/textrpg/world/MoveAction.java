package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.lib.util.Util;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.EnumAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;

/**
 * Action to traverse an exit.
 * @author Sarge
 */
@EnumAction(Direction.class)
public class MoveAction extends AbstractAction {
	private static final ActionException INVALID = ActionException.of(Link.INVALID_DIRECTION);

	private final MovementController mover;

	/**
	 * Constructor.
	 * @param mover Movement controller
	 */
	public MoveAction(MovementController mover) {
		super(Flag.OUTSIDE);
		this.mover = notNull(mover);
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
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
	 * Moves the actor.
	 * @param actor 	Actor
	 * @param dir		Direction to move
	 * @throws ActionException if the actor cannot traverse the exit
	 */
	@RequiresActor
	public Response move(Entity actor, Direction dir) throws ActionException {
		// Check exit is available
		final Exit exit = actor.location()
			.exits()
			.find(dir)
			.filter(e -> e.isPerceivedBy(actor))
			.orElseThrow(() -> INVALID);

		// Check exit can be traversed
		exit.link().reason(actor).map(ActionException::new).ifPresent(Util::rethrow);

		// Traverse exit
		interrupt(actor);
		final var results = mover.move(actor, exit, 1);

		// Move group
		// TODO
		// - if leader
		// - followers as well? do we actually need followers, just use group? => group::merge?

		// Build response
		final var response = new Response.Builder().display();
		results.stream().map(MovementController.Result::description).flatMap(Optional::stream).forEach(response::add);
		exit.link().message().ifPresent(response::add);
		// TODO - tired warnings (also -> prompt)
		return response.build();
	}
}
