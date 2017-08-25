package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;

/**
 * Rope that can be attached to an {@link Anchor} and climbed.
 * @author Sarge
 */
public class Rope extends DurableObject {
	/**
	 * Anchor for a rope.
	 */
	public static class Anchor extends WorldObject {               // TODO - extends Fixture
		private Rope rope;

		/**
		 * Constructor.
		 * @param descriptor Descriptor
		 */
		public Anchor(ObjectDescriptor descriptor) {
			super(descriptor);
		}

		@Override
		protected void take(Actor actor) throws ActionException {
			throw FIXTURE;
		}

		/**
		 * @return Whether a rope is attached to this anchor
		 */
		public boolean isAttached() {
			return rope != null;
		}

		/**
		 * @return Attached rope
		 */
		protected Rope getRope() {
			return rope;
		}
	}

	/**
	 * Rope descriptor.
	 */
	public static class Descriptor extends DurableObject.Descriptor {
		private final int length;
		private final boolean magical;

		/**
		 * Constructor.
		 * @param descriptor		Descriptor
		 * @param durability		Durability
		 * @param length			Length
		 * @param elven				Whether this is an magical rope (that can be remotely untied)
		 */
		public Descriptor(ObjectDescriptor descriptor, int durability, int length, boolean magical) {
			super(descriptor, durability);
			Check.oneOrMore(length);
			this.length = length;
			this.magical = magical;
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
	public Rope(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public String getFullDescriptionKey() {
		if(anchor == null) {
			return super.getFullDescriptionKey();
		}
		else {
			return "rope.attached";
		}
	}

	@Override
	protected void describe(Description.Builder builder) {
		super.describe(builder);
		builder.add("length", getLength());
		if(anchor != null) {
		    builder.wrap("anchor", anchor.getName());
		}
	}

	@Override
	public void take(Actor actor) throws ActionException {
		super.take(actor);
		if(anchor != null) throw new ActionException("take.rope.attached");
	}

	/**
	 * @return Length of this rope
	 */
	public int getLength() {
		final Descriptor descriptor = (Descriptor) super.getDescriptor();
		return descriptor.length;
	}

	/**
	 * @return Whether this is a magical rope
	 */
	public boolean isMagical() {
		final Descriptor descriptor = (Descriptor) super.getDescriptor();
		return descriptor.magical;
	}

	/**
	 * @return Current anchor of this rope if any
	 */
	public Optional<Anchor> getAnchor() {
		return Optional.ofNullable(anchor);
	}

	/**
	 * Attaches this rope to the given anchor.
	 * @param anchor Anchor object
	 * @throws ActionException if the given object is not a valid anchor or this rope is already attached to something
	 */
	protected void attach(Actor actor, Anchor anchor) throws ActionException {
		if(this.anchor != null) throw new ActionException("rope.already.attached");
		if(anchor.isAttached()) throw new ActionException("rope.anchor.occupied");
		if(isBroken()) throw new ActionException("rope.attach.broken");
		this.setParentAncestor(actor.getParent());
		this.anchor = anchor;
		anchor.rope = this;
	}

	/**
	 * Removes this rope from its anchor.
	 * @param actor Actor
	 * @throws ActionException if this rope is not attached or is attached to an anchor in a different location
	 */
	protected void remove(Actor actor) throws ActionException {
		if(anchor == null) throw new ActionException("rope.not.attached");
		if(anchor.rope.getParent() != actor.getParent()) throw new ActionException("rope.invalid.location");
		this.setParent(actor);
		anchor.rope = null;
		anchor = null;
	}

	/**
	 * Pulls this magical rope and returns it to actors inventory.
	 * @param actor Actor
	 * @throws ActionException if the rope is not attached or cannot be pulled
	 */
	protected void pull(Actor actor) throws ActionException {
		if(anchor == null) throw new ActionException("rope.not.attached");
		if(anchor.rope.getParent() == actor.getParent()) throw new ActionException("rope.invalid.location");
		if(!isMagical()) throw new ActionException("interact.nothing");
		this.setParent(actor);
		anchor.rope = null;
		anchor = null;
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
