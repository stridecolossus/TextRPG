package org.sarge.textrpg.object;

import java.util.EnumSet;
import java.util.Set;

import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.entity.Stance;

/**
 * Furniture object that can be occupied by an {@link Entity}.
 * @author Sarge
 */
public class Furniture extends WorldObject implements Parent {
	/**
	 * Furniture parent name.
	 */
	public static final String NAME = "furniture";

	/**
	 * Furniture descriptor.
	 * @author Sarge
	 */
	public static class Descriptor extends ContentsObjectDescriptor {
		private final Set<Stance> stances;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param stances			Valid stances for this furniture
		 */
		public Descriptor(ContentsObjectDescriptor descriptor, Set<Stance> stances) {
			super(descriptor);
			this.stances = EnumSet.copyOf(stances);
		}

		/**
		 * @param stance Stance
		 * @return Whether the given stance is valid for this furniture
		 */
		public boolean isValid(Stance stance) {
			return stances.contains(stance);
		}

		@Override
		public WorldObject create() {
			return new Furniture(this);
		}
	}

	private final TrackedContents contents;

	/**
	 * Constructor.
	 * @param descriptor Furniture descriptor
	 */
	public Furniture(Descriptor descriptor) {
		super(descriptor);
		this.contents = new TrackedContents(descriptor.limits);
	}

	@Override
	public String parentName() {
		return NAME;
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
	public boolean isSentient() {
		return true;
	}

	@Override
	public void alert(Notification n) {
		contents.stream().filter(Thing::isSentient).forEach(t -> t.alert(n));
	}
}
