package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * Control handler that sets the state of an openable object.
 * @author Sarge
 */
public class OpenableControlHandler extends AbstractEqualsObject implements Control.Handler {
	private final Openable.Model model;
	private final Openable.State state;
	private final String message;

	/**
	 * Constructor.
	 * @param portal 		Controlled portal
	 * @param state			Openable state when activated
	 * @param message		Toggle message prefix
	 * @throws IllegalArgumentException if the target state is {@link Openable.State#LOCKED} but the openable is not lockable
	 */
	public OpenableControlHandler(Openable openable, Openable.State state, String message) {
		this.model = notNull(openable.model());
		this.state = notNull(state);
		this.message = notEmpty(message);
		verify();
	}

	private void verify() {
		if(!model.isLockable() && (state == Openable.State.LOCKED)) {
			throw new IllegalArgumentException("Cannot lock an unlockable openable model");
		}
	}

	@Override
	public Description handle(Actor actor, Control control, boolean activated) {
		// Update portal state
		if(activated) {
			model.set(state);
		}
		else {
			model.reset();
		}

		// Build response
		final String result = TextHelper.join(message, String.valueOf(activated));
		return Description.of(result);
	}
}
