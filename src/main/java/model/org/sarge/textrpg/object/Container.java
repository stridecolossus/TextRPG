package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.Optional;
import java.util.Set;

import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.object.ObjectDescriptor.Equipment;
import org.sarge.textrpg.object.TrackedContents.Limit;

/**
 * Container object such as a chest, bookcase or arrow quiver.
 * @author Sarge
 * @see TrackedContents.Limit
 */
public class Container extends WorldObject implements Parent {
	/**
	 * Descriptor for this container.
	 */
	public static class Descriptor extends ContentsObjectDescriptor {
		private final String placement;
		private final Optional<Openable.Lock> lock;
		private final Optional<DeploymentSlot> slot;

		/**
		 * Constructor.
		 * @param descriptor		Descriptor for this container
		 * @param placement         Container placement identifier
		 * @param lock				Optional descriptor for an openable container
		 * @param slot				Optional deployment slot for containers that than carry equipment such as a key-ring or belt
		 */
		public Descriptor(ContentsObjectDescriptor descriptor, String placement, Openable.Lock lock, DeploymentSlot slot) {
			super(descriptor);
			this.placement = notEmpty(placement);
			this.lock = Optional.ofNullable(lock);
			this.slot = Optional.ofNullable(slot);
			if(lock != null) {
			    verifyResetable();
			}
		}

		@Override
		public String getDescriptionKey() {
			return "container";
		}

		/**
		 * @return Container placement
		 */
		public String getPlacement() {
			return placement;
		}

		/**
		 * @return Deployment slot for containers that than carry equipment such as a key-ring or belt
		 */
		public Optional<DeploymentSlot> getContentsDeploymentSlot() {
			return slot;
		}

		@Override
		public Container create() {
			return new Container(this);
		}
	}

	/**
	 * Creates a category-based limit.
	 * @param cats Categories
	 * @return Category limit
	 */
	public static Limit categoryLimit(Set<String> cats) {
		return (t, c) -> {
			final WorldObject obj = (WorldObject) t;
			return obj.getDescriptor().getCharacteristics().getCategories().noneMatch(cats::contains);
		};
	}

	/**
	 * Contents of this container.
	 */
	private class ContainerContents extends TrackedContents {
		private ContainerContents() {
			super(getDescriptor().limits);
		}

		@Override
		public String getReason(Thing t) {
			// Check open
			final boolean open = model.map(Openable::isOpen).orElse(true);
			if(!open) return "container.add.closed";

			// Check specialist equipment containers
			final Descriptor descriptor = getDescriptor();
			if(descriptor.slot.isPresent()) {
				final DeploymentSlot slot = descriptor.slot.get();
				final WorldObject obj = (WorldObject) t;
				final boolean valid = obj.getDescriptor().getEquipment().map(Equipment::getDeploymentSlot).map(slot::equals).orElse(false);
				if(!valid) {
					return "container.add." + descriptor.slot.get().name();
				}
			}

			return null;
		}

		@Override
		public void add(Thing obj) {
			// TODO - register contents with emissions? e.g. can see light through glass? can smell cheese in an open box?
			super.add(obj);
		}

		@Override
		protected void move(Parent parent) {
			super.move(parent);
		}
	}

	private final ContainerContents contents = new ContainerContents();
	private final Optional<Openable> model;

	/**
	 * Constructor.
	 * @param descriptor Container descriptor
	 */
	public Container(Descriptor descriptor) {
		super(descriptor);
		this.model = descriptor.lock.map(Openable::new);
	}

	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.getDescriptor();
	}

	@Override
	public int weight() {
		return super.weight() + contents.getWeight();
	}

	@Override
	public Optional<Openable> getOpenableModel() {
		return model;
	}

	@Override
	protected void describe(Description.Builder builder) {
		super.describe(builder);
		final boolean open = model.map(Openable::isOpen).orElse(false);
		if(open) {
			builder.wrap("container.open", "container.open");
		}
	}

	@Override
	public TrackedContents getContents() {
		return contents;
	}

	/**
	 * Empties the contents of this container to the given parent.
	 */
	protected void empty(Parent parent) {
		contents.move(parent);
	}

	@Override
	protected void destroy() {
		move(this.getParent());
		super.destroy();
	}
}
