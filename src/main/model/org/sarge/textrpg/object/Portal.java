package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.StringUtils;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.TextHelper;

/**
 * A <i>portal</i> is an openable object that controls an exit such as a door or field-gate.
 * <p>
 * Notes:
 * <ul>
 * <li>A <i>quiet</i> portal is omitted from the enumerated exits of a location and can be used to model <i>fake</i> or secret doors, e.g. a bookcase that is actually a door.</li>
 * <li>Portals can be broken (i.e. destroyed) and blocked, see {@link #block()}</li>
 * </ul>
 * @author Sarge
 */
public class Portal extends WorldObject implements Openable {
	/**
	 * Portal states.
	 */
	public enum PortalState {
		DEFAULT,
		BROKEN,
		BLOCKED
	}

	/**
	 * Openable model for this portal.
	 */
	private class PortalModel extends Openable.Model {
		private PortalState state = PortalState.DEFAULT;

		/**
		 * Constructor.
		 * @param lock Lock descriptor
		 */
		private PortalModel(Openable.Lock lock) {
			super(lock);
		}

		@Override
		public boolean isOpen() {
			switch(state) {
			case BLOCKED:		return false;
			case BROKEN:		return true;
			default:			return super.isOpen();
			}
		}

		@Override
		public void apply(Operation op) throws Openable.OpenableException {
			if(state == PortalState.DEFAULT) {
				super.apply(op);
			}
			else {
				throw new OpenableException(TextHelper.join("portal", state.name()));
			}
		}

		/**
		 * Blocks or breaks this portal.
		 * @param state New state
		 */
		private void update(PortalState state) {
			assert state != this.state;
			this.state = state;
		}

		@Override
		public void reset() {
			super.reset();
			state = PortalState.DEFAULT;
		}
	}

	/**
	 * Portal descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Openable.Lock lock;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param lock				Lock descriptor
		 */
		public Descriptor(ObjectDescriptor descriptor, Openable.Lock lock) {
			super(descriptor);
			this.lock = notNull(lock);
		}

		@Override
		public final boolean isFixture() {
			return true;
		}

		@Override
		public final boolean isResetable() {
			return true;
		}

		@Override
		public final Portal create() {
			return new Portal(this);
		}
	}

	private final PortalModel model;

	/**
	 * Constructor.
	 * @param descriptor Portal descriptor
	 */
	protected Portal(Descriptor descriptor) {
		super(descriptor);
		model = new PortalModel(descriptor.lock);
	}

	@Override
	public Openable.Model model() {
		return model;
	}

	/**
	 * @return Current state of this portal
	 */
	PortalState state() {
		return model.state;
	}

	@Override
	protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
		assert !carried;
		if(model.state == PortalState.BROKEN) {
			builder.add(KEY_STATE, "portal.broken");
		}
		else
		if(model.isOpen()) {
			builder.add(KEY_STATE, StringUtils.EMPTY);
		}
		else {
			builder.add(KEY_STATE, "portal.closed");
		}
		super.describe(carried, builder, formatters);
	}

	@Override
	public void damage(Damage.Type type, int amount) {
		if(model.state != PortalState.BROKEN) {
			super.damage(type, amount);
		}
	}

	/**
	 * Blocks this portal.
	 * @throws IllegalStateException if this portal cannot be blocked
	 */
	void block() {
		if(model.state != PortalState.DEFAULT) throw new IllegalStateException("Portal cannot be blocked: " + this);
		model.update(PortalState.BLOCKED);
	}

	@Override
	protected void destroy() {
		model.update(PortalState.BROKEN);
	}
}
