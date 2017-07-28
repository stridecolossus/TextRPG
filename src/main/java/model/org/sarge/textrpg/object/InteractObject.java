package org.sarge.textrpg.object;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Event;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;

/**
 * Object that can be manipulated.
 * @author Sarge
 */
public class InteractObject extends WorldObject {
	/**
	 * Queue for control reset events.
	 */
	private static final EventQueue QUEUE = new EventQueue();

	/**
	 * Descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Set<Interaction> interactions;
		private final int str;
		private final boolean removes;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param interactions		Interactions
		 * @param str				Required strength (zero-or-more)
		 * @param removes			Whether interaction removes this object
		 */
		public Descriptor(ObjectDescriptor descriptor, Set<Interaction> interactions, int str, boolean removes) {
			super(descriptor);
			Check.zeroOrMore(str);
			if(interactions.contains(Interaction.EXAMINE)) {
				throw new IllegalArgumentException("EXAMINE invalid for an interact object");
			}
			verifyResetable();
			this.interactions = EnumSet.copyOf(interactions);
			this.str = str;
			this.removes = removes;
		}

		@Override
		public InteractObject create() {
			return new InteractObject(this);
		}
	}

	private final Openable model = new Openable(Openable.FIXED);

	/**
	 * Constructor.
	 * @param descriptor Descriptor
	 */
	public InteractObject(ObjectDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Optional<Openable> getOpenableModel() {
		return Optional.of(model);
	}

	/**
	 * @return Strength required to interact with this object
	 */
	public int getRequiredStrength() {
		final Descriptor descriptor = (Descriptor) super.descriptor;
		return descriptor.str;
	}

	/**
	 * Interacts with this object.
	 * @param action	Interaction
	 * @param parent	Parent location
	 * @throws ActionException if the action is invalid or the object has already been interacted with
	 */
	protected void interact(Interaction action, Parent parent) throws ActionException {
		// Check can be interacted with
		final Descriptor descriptor = (Descriptor) super.descriptor;
		if(!descriptor.interactions.contains(action)) {
			throw new ActionException("interact.invalid.action");
		}
		if(model.isOpen()) {
			throw new ActionException("interact.done." + action);
		}

		// Interact
		model.setOpen(true);

		// Hide
		if(descriptor.removes) {
			hide();
		}

		// Register reset event
		final Event reset = () -> {
			model.setOpen(false);
			if(descriptor.removes) {
				setParentAncestor(parent);
			}
		};
		QUEUE.add(reset, descriptor.getProperties().getResetPeriod());
	}
}
