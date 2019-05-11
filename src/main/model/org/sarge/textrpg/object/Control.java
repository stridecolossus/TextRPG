package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;

/**
 * A <i>control</i> is an object that can be manipulated by the actor, e.g. a lever, bell-rope, or an object that reveals a previously hidden object.
 * @author Sarge
 */
public class Control extends WorldObject {
	/**
	 * Ignored interaction.
	 */
	public static final ActionException IGNORED = ActionException.of("control.interaction.none");

	/**
	 * Callback handler for control state changes.
	 */
	@FunctionalInterface
	public interface Handler {
		/**
		 * Handles a control state change.
		 * @param actor			Actor
		 * @param control		Control
		 * @param activated		Whether de/activated
		 * @return Response description
		 */
		Description handle(Actor actor, Control control, boolean activated);
	}

	/**
	 * Interaction policy.
	 */
	public enum Policy {
		/**
		 * Interaction does not affect this control.
		 */
		DEFAULT,

		/**
		 * Toggles the state of this control, e.g. a lever.
		 */
		TOGGLE,

		/**
		 * Interacting hides this control until reset.
		 */
		HIDES
	}

	private final Interaction interaction;
	private final Handler handler;
	private final Policy policy;

	private boolean activated;

	/**
	 * Constructor.
	 * @param descriptor		Object descriptor
	 * @param interaction		Interaction of this control
	 * @param handler			State-change handler
	 * @param policy			Interaction policy
	 * @throws IllegalArgumentException if the descriptor is not valid for a control
	 */
	public Control(ObjectDescriptor descriptor, Interaction interaction, Handler handler, Policy policy) {
		super(descriptor);
		this.interaction = notNull(interaction);
		this.handler = notNull(handler);
		this.policy = notNull(policy);
		verify(descriptor);
	}

	/**
	 * Verifies this control.
	 */
	private void verify(ObjectDescriptor descriptor) {
		if(!descriptor.isFixture()) throw new IllegalArgumentException("Control descriptor must be a fixture: " + descriptor);
		if(descriptor.properties().reset().isZero()) throw new IllegalArgumentException("Control descriptor must be resetable: " + descriptor);
		if((interaction == Interaction.EXAMINE) && (policy != Policy.DEFAULT)) throw new IllegalArgumentException("Invalid policy for examine control: " + this);
	}

	/**
	 * @return Interaction for this control
	 */
	public Interaction interaction() {
		return interaction;
	}

	@Override
	public boolean isAlive() {
		if(activated && (policy == Policy.HIDES)) {
			return false;
		}
		else {
			return super.isAlive();
		}
	}

	/**
	 * Interacts with this control.
	 * @param actor			Actor
	 * @param interaction 	Type of interaction
	 * @return Response
	 * @throws ActionException if the interaction is not valid for this control
	 */
	public Description interact(Actor actor, Interaction action) throws ActionException {
		// Toggle state
		if(activated) {
			// Deactivate control
			deactivate(actor, action);
			activated = false;
		}
		else {
			// Activate control
			if(interaction != action) {
				throw IGNORED;
			}
			activated = true;
		}

		// Delegate to handler and build response
		return handler.handle(actor, this, activated);
	}

	/**
	 * Deactivates this control.
	 * @param actor			Actor
	 * @param interaction 	Type of interaction
	 * @throws ActionException if the interaction is not valid or the control cannot be toggled
	 */
	private void deactivate(Actor actor, Interaction action) throws ActionException {
		if((policy == Policy.TOGGLE) && (action == interaction.invert())) {
			// Valid toggle
			return;
		}
		else
		if(action == interaction) {
			// Duplicate state
			throw ActionException.of("control.already", interaction.name());
		}
		else {
			// Ignored
			throw IGNORED;
		}
	}

	/**
	 * Resets this control.
	 */
	public void reset() {
		handler.handle(null, this, false);
		activated = false;
	}
}
