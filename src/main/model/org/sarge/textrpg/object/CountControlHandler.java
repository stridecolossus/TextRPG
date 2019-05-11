package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.object.Control.Handler;
import org.sarge.textrpg.util.Description;

/**
 * A count handler tracks the number of control activations and delegates when a target count is achieved, e.g. press a number of panels to solve a puzzle.
 * @author Sarge
 */
public class CountControlHandler extends AbstractEqualsObject implements Control.Handler {
	private final Control.Handler handler;
	private final int max;

	private int count;

	/**
	 * Constructor.
	 * @param handler		Delegate handler
	 * @param max			Target count
	 */
	public CountControlHandler(Handler handler, int max) {
		this.handler = notNull(handler);
		this.max = oneOrMore(max);
	}

	@Override
	public Description handle(Actor actor, Control control, boolean activated) {
		if(activated) {
			++count;
			if(count == max) {
				return handler.handle(actor, control, true);
			}
		}
		else {
			if(count == max) {
				return handler.handle(actor, control, false);
			}
			--count;
		}
		assert (count >= 0) && (count < max);

		return null; // TODO - empty?
	}
}
