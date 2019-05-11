package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.Map;
import java.util.Set;

import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents;
import org.sarge.textrpg.contents.LimitedContents.Limit;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

/**
 * Furniture that can occupied by an actor.
 * @author Sarge
 */
public class Furniture extends WorldObject implements Parent {
	/**
	 * Furniture descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Set<Stance> stances;
		private final LimitsMap limits;
		private final String placement;

		/**
		 * Constructor.
		 * @param descriptor		Furniture descriptor
		 * @param stances			Valid stances for this furniture
		 * @param capacity			Capacity of this furniture
		 * @param placement			Placement key
		 */
		public Descriptor(ObjectDescriptor descriptor, Set<Stance> stances, int capacity, String placement) {
			super(descriptor);
			this.stances = Set.copyOf(notEmpty(stances));
			this.limits = new LimitsMap(Map.of(
				"furniture.max.occupants",		Limit.capacity(capacity),
				"furniture.size.constraint",	Limit.size(descriptor.properties().size())
			));
			this.placement = notEmpty(placement);
		}

		/**
		 * @param stance Stance
		 * @return Whether the given stance is valid for this furniture
		 */
		public boolean isValid(Stance stance) {
			return stances.contains(stance);
		}

		@Override
		public Furniture create() {
			return new Furniture(this);
		}
	}

	private final Contents contents;

	/**
	 * Constructor.
	 * @param descriptor Furniture descriptor
	 */
	protected Furniture(Descriptor descriptor) {
		super(descriptor);
		contents = new LimitedContents(descriptor.limits) {
			@Override
			public String placement() {
				return descriptor.placement;
			}
		};
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	@Override
	public int weight() {
		if(contents.isEmpty()) {
			return super.weight();
		}
		else {
			return ObjectDescriptor.FIXTURE;
		}
	}

	@Override
	public Contents contents() {
		return contents;
	}

	@Override
	public boolean notify(ContentStateChange notification) {
		return true;
	}
}
