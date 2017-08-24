package org.sarge.textrpg.object;

import java.util.logging.Level;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Openable.Operation;
import org.sarge.textrpg.common.Script;

/**
 * Script to manipulate an {@link Openable}.
 * @author Sarge
 * TODO - need to split this into openable (world object) and portal/gate so can notify other side of links
 */
public class PortalScript implements Script {
	private final WorldObject obj;
	private final Openable.Operation op;
	
	/**
	 * Constructor.
	 * @param model		Openable object
	 * @param op		Operation
	 */
	public PortalScript(WorldObject obj, Operation op) {
		Check.notNull(op);
		if(!obj.getOpenableModel().isPresent()) throw new IllegalArgumentException("Not an openable object");
		this.obj = obj;
		this.op = op;
	}
	
	@Override
	public void execute(Actor actor) {
		// Perform operation
		final Openable model = obj.getOpenableModel().get();
		if(model.getState() != op.getState()) {
			try {
				model.apply(op);
			}
			catch(ActionException e) {
				LOG.log(Level.SEVERE, String.format("Portal script failed: model=%s op=%s", model, op), e);
			}

			// Register reset event
			// TODO
			//ActionHelper.registerOpenableEvent(queue, actor.root(), null, obj, "portal.auto.close");
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
