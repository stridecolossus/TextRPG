package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.world.Terrain;
import org.springframework.stereotype.Component;

/**
 * Interrupts the current induction.
 * @author Sarge
 */
@Component
public class StopAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public StopAction() {
		super(Flag.OUTSIDE);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	public boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Interrupts the current <i>active</i> induction.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if there is no currently active induction to interrupt
	 */
	@RequiresActor
	public Response stop(Entity actor) throws ActionException {
		final Induction.Manager manager = actor.manager().induction();
		if(!manager.isActive()) throw ActionException.of("stop.cannot.interrupt");
		manager.interrupt();
		return Response.EMPTY;
	}
}
