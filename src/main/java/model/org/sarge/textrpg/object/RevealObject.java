package org.sarge.textrpg.object;

import java.util.EnumSet;
import java.util.Set;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Event;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.util.Percentile;

/**
 * Object that reveals another object when examined.
 * @author Sarge
 * TODO
 * - this is very messy, separate into revealer for fixtures, for links (?), for any Hidden?, and for 'dispensed' objects
 * - examine | push | pill | move
 */
public class RevealObject extends WorldObject {
	/**
	 * Queue for reveal reset events.
	 */
	protected static final EventQueue QUEUE = new EventQueue();

	/**
	 * Descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Set<Interaction> interactions;
		private final ObjectDescriptor delegate;
		private final boolean replaces;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param interactions		Interactions
		 * @param delegate			Descriptor for revealed object
		 * @param replaces			Whether the revealed object <i>replaces</i> this object
		 */
		public Descriptor(ObjectDescriptor descriptor, Set<Interaction> interactions, ObjectDescriptor delegate, boolean replaces) {
			super(descriptor);
			Check.notNull(delegate);
			verifyResetable();
			this.interactions = EnumSet.copyOf(interactions);
			this.delegate = delegate;
			this.replaces = replaces;
		}

		@Override
		public WorldObject create() {
			return new RevealObject(this);
		}
	}

	private boolean revealed;
	private WorldObject obj;

	/**
	 * Constructor.
	 * @param descriptor Descriptor
	 */
	public RevealObject(Descriptor descriptor) {
		super(descriptor);
		if(descriptor.delegate.isFixture()) {
			obj = descriptor.delegate.create();
		}
	}

	/**
	 * @return Whether the concealed object has been revealed
	 */
	public boolean isRevealed() {
		return revealed;
	}

	/**
	 * @param action Interaction
	 * @return Whether the given interaction reveals this object
	 */
	public boolean isInteraction(Interaction action) {
		final Descriptor descriptor = (Descriptor) super.descriptor;
		return descriptor.interactions.contains(action);
	}

	@Override
	public Percentile getVisibility() {
		final Descriptor descriptor = (Descriptor) super.descriptor;
		if(revealed && descriptor.replaces)
			return Percentile.ZERO;
		else
			return super.getVisibility();
	}

	@Override
	public void setParent(Parent parent) throws ActionException {
		super.setParent(parent);
		if(obj != null) {
			obj.setParent(parent);
		}
	}

	/**
	 * Reveals the concealed object if not already revealed.
	 * @return Revealed object
	 * @throws IllegalStateException if already revealed
	 */
	protected WorldObject reveal() {
		if(revealed) throw new IllegalStateException("Already revealed");

		// Create new instance if not a fixture
		final Descriptor descriptor = (Descriptor) super.descriptor;
		final ObjectDescriptor delegate = descriptor.delegate;
		final boolean fixed = delegate.isFixture();
		if(!fixed) {
			assert obj == null;
			obj = descriptor.delegate.create();
			obj.setParentAncestor(this.getParent());
		}

		// Reveal
		revealed = true;

		// Register reset event
		final Event event = () -> {
			// Hide or remove revealed object
			if(fixed) {
				// Hide fixture
				obj.hide();
			}
			else
			if(obj.getParent() == this.getParent()) {
				// Destroy non-fixture if not taken
				obj.destroy();
				obj = null;
			}

			// Restore this object
			revealed = false;
		};
		QUEUE.add(event, descriptor.getProperties().getResetPeriod());

		return obj;
	}
}
