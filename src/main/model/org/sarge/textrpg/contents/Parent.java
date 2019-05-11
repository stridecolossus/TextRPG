package org.sarge.textrpg.contents;

/**
 * A <i>parent</i> is a container for objects or entities.
 */
public interface Parent {
	/**
	 * Limbo parent.
	 */
	Parent LIMBO = new Parent() {
		private final Contents contents = new LimboContents();

		@Override
		public String name() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Parent parent() {
			return null;
		}

		@Override
		public Contents contents() {
			return contents;
		}
	};

	/**
	 * @return Contents of this parent
	 */
	Contents contents();

	/**
	 * @return Name of this parent
	 */
	String name();

	/**
	 * @return Parent of this object or <tt>null</tt> if a root object
	 */
	Parent parent();

	/**
	 * @return Whether this is a sentient parent
	 */
	default boolean isSentient() {
		return false;
	}

	/**
	 * Propagates a state-change notification to the ancestors of this parent.
	 * @param notification Notification
	 * @return Whether to continue to propagate (default is <tt>true</tt>)
	 */
	default boolean notify(ContentStateChange notification) {
		return true;
	}
}
