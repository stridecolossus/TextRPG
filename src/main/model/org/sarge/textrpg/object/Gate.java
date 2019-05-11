package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Link;

/**
 * A <i>gate</i> is a portal that cannot be manipulated directly by an actor, such as a town-gate or draw-bridge.
 * @author Sarge
 */
public class Gate extends WorldObject {
	/**
	 * Gate-keeper descriptor.
	 */
	public static final class Keeper extends AbstractEqualsObject {
		private final String name;
		private final Faction.Association relationship;
		private final Optional<Integer> bribe;

		/**
		 * Constructor.
		 * @param name				Gate-keeper name
		 * @param association		Minimum association with the controlling faction required in order to call for this gate to be opened
		 * @param bribe				Minimum bribe level of this gate-keeper (optional)
		 */
		public Keeper(String name, Faction.Association association, Integer bribe) {
			if(association.relationship() == Relationship.ENEMY) throw new IllegalArgumentException("Invalid minimum faction relationship");
			this.name = notEmpty(name);
			this.relationship = notNull(association);
			this.bribe = Optional.ofNullable(bribe);
		}

		/**
		 * @return Gate-keeper name
		 */
		public String name() {
			return name;
		}

		/**
		 * @return Minimum relationship required to call for this gate to be opened
		 */
		public Faction.Association association() {
			return relationship;
		}

		/**
		 * @return Minimum bribe amount
		 */
		public Optional<Integer> bribe() {
			return bribe;
		}
	}

	/**
	 * Descriptor for a gate.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Optional<Keeper> keeper;
		private boolean open;

		/**
		 * Constructor.
		 * @param descriptor		Gate descriptor
		 * @param keeper			Optional gate-keeper descriptor
		 */
		public Descriptor(ObjectDescriptor descriptor, Keeper keeper) {
			super(descriptor);
			this.keeper = Optional.ofNullable(keeper);
		}

		@Override
		public final boolean isFixture() {
			return true;
		}

		@Override
		public final boolean isResetable() {
			return true;
		}

		/**
		 * @return Gate-keeper descriptor
		 */
		public Optional<Keeper> keeper() {
			return keeper;
		}

		/**
		 * Sets whether <b>all</b> gates with this descriptor are open or closed.
		 * @param open Whether open or closed
		 */
		public void setOpen(boolean open) {
			this.open = open;
		}

		@Override
		public Gate create() {
			return new Gate(this);
		}
	}

	/**
	 * Link for this gate.
	 */
	// TODO - route?
	private final Link link = new Link() {
		private final Optional<Description> reason = Optional.of(new Description("portal.closed", Gate.this.name()));

		@Override
		public Optional<Thing> controller() {
			return Optional.of(Gate.this);
		}

		@Override
		public boolean isTraversable() {
			return isOpen();
		}

		@Override
		public Optional<Description> reason(Thing actor) {
			if(isOpen()) {
				return super.reason(actor);
			}
			else {
				return reason;
			}
		}

		@Override
		public String wrap(String dir) {
			return PortalLink.wrap(dir, isOpen());
		}
	};

	private final Event.Holder reset = new Event.Holder();

	private boolean called;

	/**
	 * Constructor.
	 * @param descriptor Gate descriptor
	 */
	protected Gate(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	/**
	 * @return Holder for reset event
	 */
	Event.Holder holder() {
		return reset;
	}

	/**
	 * @return Link descriptor for this gate
	 */
	public Link link() {
		return link;
	}

	@Override
	public boolean isQuiet() {
		return true;
	}

	/**
	 * @return Whether this gate is currently open
	 */
	public boolean isOpen() {
		final Descriptor descriptor = this.descriptor();
		return descriptor.open == !called;
	}

	/**
	 * Calls for this gate to be opened or closed.
	 * @throws IllegalStateException if the gate does not have a gate-keeper
	 * @throws ActionException if the gate is already open (during the day)
	 */
	void call() throws ActionException {
		// Check can be called
		final Descriptor descriptor = this.descriptor();
		if(!descriptor.keeper.isPresent()) throw new IllegalStateException("Cannot call a gate without a gate-keeper");
		if(descriptor.open) throw ActionException.of("gate.call.daytime");

		if(called) {
			// Call to close
			reset.cancel();
			called = false;
		}
		else {
			// Call to open
			called = true;
		}
	}

	/**
	 * Resets this gate.
	 */
	void reset() {
		called = false;
	}
}
