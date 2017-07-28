package org.sarge.textrpg.object;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.sarge.lib.util.StrictMap;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.TrackedContents.Limit;

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
	public static class Descriptor extends ObjectDescriptor {
		private final Set<Stance> stances;
		private final Map<Limit, String> limits;
		
		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param stances			Valid stances for this furniture
		 * @param limits			Contents limits
		 */
		public Descriptor(ObjectDescriptor descriptor, Set<Stance> stances, Map<Limit, String> limits) {
			super(descriptor);
			this.stances = EnumSet.copyOf(stances);
			this.limits = new StrictMap<>(limits);
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
	public String getParentName() {
		return NAME;
	}
	
	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.getDescriptor();
	}
	
	@Override
	public Contents getContents() {
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
