package org.sarge.textrpg.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictList;

/**
 * Mutable set of contents.
 * @author Sarge
 */
public class Contents {
	/**
	 * Immutable set of contents.
	 */
	public static final Contents IMMUTABLE = new Contents() {
		@Override
		public void add(Thing obj) {
			throw new RuntimeException();
		}

		@Override
		protected void remove(Thing obj) {
			// Does nowt
		}
	};

	/**
	 * Listener for add/remove events on this set of contents.
	 */
	public static interface Listener {
		/**
		 * Notifies when this set of contents is modified.
		 * @param add Add/remove
		 * @param obj Object
		 */
		void contentsChanged(boolean add, Thing obj);
	}

	protected final List<Thing> contents = new StrictList<>();
	private final List<Listener> listeners = new StrictList<>();

	/**
	 * @return Contents
	 */
	public final Stream<Thing> stream() {
		return contents.stream();
	}

	/**
	 * @param depth Maximum depth
	 * @return Recursive contents
	 */
	public final Stream<Thing> stream(int depth) {
		Check.zeroOrMore(depth);
		final Stream.Builder<Thing> stream = Stream.builder();
		stream(this, stream, depth);
		return stream.build();
	}

	/**
	 * Recursively builds a stream from this set of contents.
	 */
	private static void stream(Contents c, Stream.Builder<Thing> stream, int depth) {
		// Add this set of contents
		c.contents.stream().forEach(stream::accept);
		
		// Recurse to children
		if(depth > 0) {
			c.contents.stream()
				.filter(t -> t instanceof Parent)
				.map(t -> (Parent) t)
				.forEach(p -> stream(p.getContents(), stream, depth - 1));
		}
	}

	/**
	 * @return Size of contents
	 */
	public final int size() {
		return contents.size();
	}
	
	/**
	 * @return Total weight of this set of contents (default implementation is zero)
	 */
	public int getWeight() {
		return 0;
	}
	
	/**
	 * Tests whether the given object can be added to this set of contents.
	 * @param obj Object to add
	 * @return Reason code if cannot be added or <tt>null</tt>
	 */
	public String getReason(Thing obj) {
		return null;
	}

	/**
	 * Adds an object to this set of contents.
	 * @param obj Object to add
	 * TODO - public
	 */
	public void add(Thing obj) {
		assert !contents.contains(obj);
		contents.add(obj);
		listeners.forEach(listener -> listener.contentsChanged(true, obj));
	}
	
	/**
	 * Removes an object from this set of contents.
	 * @param obj Object to remove
	 */
	protected void remove(Thing obj) {
		assert contents.contains(obj);
		contents.remove(obj);
		listeners.forEach(listener -> listener.contentsChanged(false, obj));
	}

	/**
	 * Adds a listener to this set of contents.
	 * @param listener Listener
	 */
	public void add(Listener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Moves this set of contents to the given parent.
	 */
	protected void move(Parent parent) {
		new ArrayList<>(contents).stream().forEach(t -> t.setParentAncestor(parent));
	}

	@Override
	public String toString() {
		return contents.toString();
	}
}
