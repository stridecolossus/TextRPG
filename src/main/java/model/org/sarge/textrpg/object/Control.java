package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.EventHolder;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Script;

/**
 * Control object with up to two states such as a button, bell-rope or lever.
 * @author Sarge
 */
public class Control extends WorldObject {
	/**
	 * Queue for control reset events.
	 */
	protected static final EventQueue QUEUE = new EventQueue();

	/**
	 * Descriptor for this control.
	 */
	public static class ControlDescriptor extends ObjectDescriptor {
		private final Interaction op;
		private final Script open;
		private final Script close;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param op				Interaction to perform on this control
		 * @param open				Script to perform when this control is opened
		 * @param close				Script to perform when this control is closed or {@link Script#NONE} if this control has <b>one</b> state
		 */
		public ControlDescriptor(ObjectDescriptor descriptor, Interaction op, Script open, Script close) {
			super(descriptor);
			Check.notNull(op);
			Check.notNull(open);
			Check.notNull(close);
			verifyResetable();
			this.op = op;
			this.open = open;
			this.close = close;
		}

		@Override
		public boolean isFixture() {
			return true;
		}

		@Override
		public Control create() {
			return new Control(this);
		}
	}

	private final EventHolder holder = new EventHolder();

	private boolean pushed;

	/**
	 * Constructor.
	 * @param descriptor Control descriptor
	 */
	public Control(ControlDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	protected void describe(Description.Builder builder) {
		builder.add("pushed", pushed);
	}

	/**
	 * @return Whether this control has been pushed
	 */
	public boolean isPushed() {
		return pushed;
	}

	/**
	 * Performs the given operation on this control.
	 * @param op		Operation to perform
	 * @param actor		Actor applying this operation
	 * @throws ActionException if the operation cannot be performed on this control
	 */
	protected void apply(Interaction op, Actor actor) throws ActionException {
		// Check expected operation
		final ControlDescriptor control = (ControlDescriptor) descriptor;
		final Interaction expected;
		if(pushed) {
			if(control.close == Script.NONE) throw new ActionException("interact.already." + op);
			expected = control.op.invert();
		}
		else {
			expected = control.op;
		}
		if(op != expected) throw new ActionException("action.invalid.interaction", op);

		// Determine script to execute
		final Script script = pushed ? control.close : control.open;
		script.execute(actor);

		// Register reset event
		final Runnable event = () -> {
			pushed = false;
			control.close.execute(actor);
		};
		final EventQueue.Entry entry = QUEUE.add(event, control.getProperties().getResetPeriod());
		holder.set(entry);

		// Toggle state
		pushed = !pushed;
	}

	@Override
	public void take(Actor actor) throws ActionException {
		throw WorldObject.FIXTURE;
	}

	@Override
	protected void damage(DamageType type, int amount) {
		// Ignored
	}

	@Override
	protected void destroy() {
		throw new RuntimeException("Cannot destroy a control");
	}
}
