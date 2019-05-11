package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.ExtendedLink;
import org.sarge.textrpg.world.Route;

/**
 * A <i>rope</i> can be attached to an {@link Anchor} and used to traverse a {@link RopeLink}.
 * @author Sarge
 */
public class Rope extends DurableObject {
	private static final Optional<Description> REASON = Optional.of(new Description("link.requires.rope"));

	/**
	 * Anchor used to attach a rope.
	 */
	public static class Anchor extends WorldObject {
		private Rope rope;

		/**
		 * Constructor.
		 * @param name 				Anchor name
		 * @param placement			Placement key
		 */
		public Anchor(String name, String placement) {
			super(new ObjectDescriptor.Builder(name).fixture().placement(placement).build());
		}

		@Override
		public boolean isQuiet() {
			return isAttached();
		}

		/**
		 * @return Whether this anchor has an attached rope
		 */
		protected boolean isAttached() {
			return rope != null;
		}
	}

	/**
	 * Link that can <b>only</b> be traversed using a rope.
	 */
	public static class RopeLink extends ExtendedLink {
		private final Anchor anchor;

		/**
		 * Constructor.
		 * @param props			Link properties
		 * @param anchor		Anchor for this link
		 * @throws IllegalArgumentException if the route-type has been over-ridden in the provided link properties
		 */
		public RopeLink(ExtendedLink.Properties props, Anchor anchor) {
			super(props);
			if(super.route() != Route.NONE) throw new IllegalArgumentException("Rope-link cannot have a route");
			this.anchor = notNull(anchor);
		}

		@Override
		public final Route route() {
			return Route.NONE;
		}

		@Override
		public Optional<Thing> controller() {
			return Optional.of(anchor);
		}

		@Override
		public boolean isTraversable() {
			return false;
		}

		@Override
		public boolean isEntityOnly() {
			return true;
		}

		@Override
		public Optional<Description> reason(Thing actor) {
			if(anchor.isAttached()) {
				return super.reason(actor);
			}
			else {
				return REASON;
			}
		}

		@Override
		public String wrap(String dir) {
			if(anchor.isAttached()) {
				return StringUtils.wrap(dir, '|');
			}
			else {
				return super.wrap(dir);
			}
		}
	}

	/**
	 * Descriptor for a rope.
	 */
	public static class Descriptor extends DurableObject.Descriptor {
		private final int length;
		private final boolean magical;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param length			Length of this rope
		 * @param magical			Whether this is a magical rope
		 */
		public Descriptor(DurableObject.Descriptor descriptor, int length, boolean magical) {
			super(descriptor);
			this.length = oneOrMore(length);
			this.magical = magical;
		}

		/**
		 * @return Length of this rope
		 */
		public int length() {
			return length;
		}

		/**
		 * @return Whether this is a magical rope
		 */
		public boolean isMagical() {
			return magical;
		}

		@Override
		public Rope create() {
			return new Rope(this);
		}
	}

	private Anchor anchor;

	/**
	 * Constructor.
	 * @param descriptor Rope descriptor
	 */
	protected Rope(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	/**
	 * @return Anchor if this rope has been attached
	 */
	public Anchor anchor() {
		return anchor;
	}

	@Override
	public String key(boolean carried) {
		if(anchor == null) {
			return super.key(carried);
		}
		else {
			return "rope.attached";
		}
	}

	@Override
	protected void describe(boolean carried, Description.Builder builder, ArgumentFormatter.Registry formatters) {
		super.describe(carried, builder, formatters);
		if(anchor != null) {
			builder.add("rope.anchor", anchor.name());
		}
	}

	/**
	 * Attaches this rope to the given anchor.
	 * @param anchor Anchor
	 * @throws ActionException if this rope has already been attached to another anchor, the anchor is already occupied, or the rope is broken
	 */
	void attach(Anchor anchor) throws ActionException {
		if(this.anchor != null) throw ActionException.of("rope.already.attached");
		if(anchor.rope != null) throw ActionException.of("rope.anchor.occupied");
		if(isBroken()) throw ActionException.of("rope.broken");
		this.anchor = anchor;
		anchor.rope = this;
	}

	/**
	 * Removes this rope from its current anchor.
	 * @throws ActionException if this rope is not attached to an anchor
	 */
	void remove() throws ActionException {
		if(anchor == null) throw ActionException.of("rope.not.attached");
		anchor.rope = null;
		anchor = null;
		use();
	}

	@Override
	protected void destroy() {
		if(anchor != null) {
			anchor.rope = null;
			anchor = null;
		}
		super.destroy();
	}
}
