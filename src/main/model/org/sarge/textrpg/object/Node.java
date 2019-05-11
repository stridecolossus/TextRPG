package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

/**
 * A <i>node</i> is a set of resources that can be collected such as a vein of ore or a bunch of herbs.
 * @author Sarge
 */
public class Node extends WorldObject {
	/**
	 * Node descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final String res;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param res				Resource name
		 */
		public Descriptor(ObjectDescriptor descriptor, String res) {			// TODO - name is recipe/vocation name?
			super(descriptor);
			this.res = notNull(res);
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
		 * @return Resource identifier
		 */
		public String resource() {
			return res;
		}

		@Override
		public Node create() {
			return new Node(this);
		}
	}

	private boolean collected;

	/**
	 * Constructor.
	 * @param descriptor Node descriptor
	 */
	protected Node(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	/**
	 * @return Whether this node has been collected
	 */
	public boolean isCollected() {
		return collected;
	}

	@Override
	public boolean isAlive() {
		return !collected;
	}

	/**
	 * Collects this node.
	 * @throws IllegalStateException if this node has already been collected
	 */
	public void collect() {
		if(collected) throw new IllegalStateException("Node already collected: " + this);
		collected = true;
	}

	/**
	 * Resets this node.
	 */
	void reset() {
		collected = false;
	}
}
