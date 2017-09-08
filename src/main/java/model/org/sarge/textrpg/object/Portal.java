package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;

/**
 * Portal object such as a door.
 * @author Sarge
 */
public class Portal extends WorldObject {
	/**
	 * Portal descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Openable.Lock lock;

		/**
		 * Constructor.
		 * @param descriptor	Object descriptor
		 * @param lock			Optional lock
		 * @throws IllegalArgumentException if the descriptor does not define a reset period for this portal
		 */
		public Descriptor(ObjectDescriptor descriptor, Openable.Lock lock) {
			super(descriptor);
			Check.notNull(lock);
			verifyResetable();
			this.lock = lock;
		}

		@Override
		public boolean isFixture() {
			return true;
		}

		@Override
		public String getDescriptionKey() {
			return "portal";
		}

		@Override
		public WorldObject create() {
			throw new UnsupportedOperationException("Portals can only be created in a link");
		}
	}

	protected final Optional<Openable> model;
	private final Parent dest;

	/**
	 * Constructor.
	 * @param descriptor	Portal descriptor
	 * @param dest			Other side of this portal
	 */
	public Portal(Descriptor descriptor, Parent dest) {
		this(descriptor, dest, new Openable(descriptor.lock));
	}

	/**
	 * Constructor.
	 * @param descriptor	Portal descriptor
	 * @param dest			Other side of this portal
	 * @param model			Openable model
	 */
	protected Portal(Descriptor descriptor, Parent dest, Openable model) {
		super(descriptor);
		Check.notNull(dest);
		descriptor.verifyResetable();
		this.model = Optional.of(model);
		this.dest = dest;
	}

	@Override
	public Optional<Openable> openableModel() {
		return model;
	}

	/**
	 * @return Destination
	 */
	public Parent destination() {
		return dest;
	}

	@Override
	protected void describe(Description.Builder description) {
		super.describe(description);
		if(model.map(Openable::isOpen).orElse(false)) {
			description.add("portal.open", "{portal.open}");
		}
	}

	@Override
	public void take(Actor actor) throws ActionException {
		throw new ActionException("take.portal");
	}

	@Override
	protected void damage(DamageType type, int amount) {
		// Ignored
		// TODO - or can portals be destroyed? i.e. reset restores them?
	}
}
