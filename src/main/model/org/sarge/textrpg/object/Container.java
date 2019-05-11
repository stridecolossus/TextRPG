package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;
import java.util.Set;

import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents;
import org.sarge.textrpg.contents.LimitedContents.Limit;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;

/**
 * A <i>container</i> is a set of contents such as a bag, backpack or bookcase.
 * @author Sarge
 * TODO - material, e.g. glass can see contents
 */
public class Container extends WorldObject implements Parent {
	private static final Optional<String> CLOSED = Optional.of("container.closed");

	/**
	 * Container descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		/**
		 * Filter for containers.
		 */
		public static final ObjectDescriptor.Filter FILTER = desc -> desc instanceof Descriptor;

		private final String placement;
		private final LimitsMap limits;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param placement			Placement preposition
		 * @param limits			Limits on the contents of this container
		 */
		public Descriptor(ObjectDescriptor descriptor, String placement, LimitsMap limits) {
			super(descriptor);
			this.placement = notEmpty(placement);
			this.limits = notNull(limits);
		}

		@Override
		public Container create() {
			return new Container(this);
		}
	}

	/**
	 * Creates a container limit that constrains by object category.
	 * @param cats Allowed categories
	 * @return Category limit
	 * @see ObjectDescriptor#isCategory(String)
	 */
	public static Limit categoryLimit(Set<String> cats) {
		return (contents, thing) -> {
			final WorldObject obj = (WorldObject) thing;
			return cats.stream().anyMatch(obj::isCategory);
		};
	}

	/**
	 * Creates a container limit that constrains by deployment slot.
	 * @param slot Deployment slot
	 * @return Deployment slot limit
	 * @throws IllegalArgumentException if the given slot is not a container slot
	 * @see Slot#isContainer()
	 */
	public static Limit slotLimit(Slot slot) {
		if(!slot.isContainer()) throw new IllegalArgumentException("Not a container slot: " + slot);
		return (contents, thing) -> {
			final WorldObject obj = (WorldObject) thing;
			return obj.descriptor().equipment().slot() == slot;
		};
	}

	/**
	 * Container contents.
	 */
	private class ContainerContents extends LimitedContents {
		/**
		 * Constructor.
		 * @param limits Container limits
		 */
		private ContainerContents(LimitsMap limits) {
			super(limits);
		}

		@Override
		public Optional<String> reason(Thing thing) {
			if(isOpen()) {
				return super.reason(thing);
			}
			else {
				return CLOSED;
			}
		}

		@Override
		public String placement() {
			return descriptor().placement;
		}

		@Override
		public EnumerationPolicy policy() {
			if(isOpen()) {
				return EnumerationPolicy.DEFAULT;
			}
			else {
				return EnumerationPolicy.CLOSED;
			}
		}
	}

	private final Contents contents;

	/**
	 * Constructor.
	 * @param descriptor Container descriptor
	 */
	protected Container(Descriptor descriptor) {
		super(descriptor);
		this.contents = new ContainerContents(descriptor.limits);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	@Override
	public Contents contents() {
		return contents;
	}

	@Override
	public int weight() {
		return super.weight() + contents.weight();
	}

	/**
	 * @return Whether this container is open
	 */
	protected boolean isOpen() {
		return true;
	}

	@Override
	public boolean notify(ContentStateChange notification) {
		return true;
	}
}
