package org.sarge.textrpg.util;

/**
 * A <i>name store</i> maps language-neutral keys to a piece of text such as object name(s), location descriptions, etc.
 * @author Sarge
 */
public interface NameStore {
	/**
	 * Retrieves the name for the given key.
	 * @param key Key
	 * @return Name for the given key or <tt>null</tt> if not found
	 */
	String get(Object key);

	/**
	 * Tests whether a name matches <b>any</b> of the names corresponding to the given key.
	 * @param key		Key
	 * @param name		Name to match
	 * @return Whether matches any entry
	 */
	boolean matches(String key, String name);

	/**
	 * @return Whether this store is empty
	 */
	boolean isEmpty();

	/**
	 * Empty store.
	 */
	NameStore EMPTY = new NameStore() {
		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public String get(Object key) {
			return null;
		}

		@Override
		public boolean matches(String key, String name) {
			return false;
		}
	};

	/**
	 * Creates a compound name-store.
	 * <p>
	 * If either store is empty this method trivially returns the other store.
	 * <p>
	 * @param left
	 * @param right
	 * @return Compound name-store
	 */
	static NameStore of(NameStore left, NameStore right) {
		if(left == right) {
			return left;
		}
		else
		if(left.isEmpty()) {
			return right;
		}
		else
		if(right.isEmpty()) {
			return left;
		}
		else {
			return new NameStore() {
				@Override
				public boolean isEmpty() {
					return false;
				}

				@Override
				public String get(Object key) {
					final String name = left.get(key);
					if(name == null) {
						return right.get(key);
					}
					else {
						return name;
					}
				}

				@Override
				public boolean matches(String key, String name) {
					return left.matches(key, name) || right.matches(key, name);
				}
			};
		}
	}
}
