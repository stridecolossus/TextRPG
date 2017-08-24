package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.MovementController;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;

/**
 * Action to climb something.
 * @author Sarge
 */
public class ClimbAction extends AbstractAction {
	private final MovementController mover;
	
	/**
	 * Constructor.
	 * @param mover Movement controller
	 */
	public ClimbAction(MovementController mover) {
		this.mover = notNull(mover);
	}

	@Override
	protected Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
	}
	
	/**
	 * Climbs an object.
	 */
	public ActionResponse climb(Entity actor, WorldObject obj) throws ActionException {
		// Find the matching exit
		final Direction dir = actor.getLocation().getExits().entrySet().stream()
			.filter(e -> matches(e.getValue(), obj))
			.map(Map.Entry::getKey)
			.findFirst()
			.orElseThrow(() -> new ActionException(ILLOGICAL));
		
		// Traverse link
		final Description description = mover.move(actor, dir, 1, true);
		return new ActionResponse(description);
	}
	
	private static boolean matches(Exit exit, WorldObject obj) {
		return exit.getLink().getController().map(c -> c == obj).orElse(false);
	}
}
