package org.sarge.textrpg.contents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.StreamUtil;

/**
 * Basic set of contents.
 * @author Sarge
 */
public class Contents {
	/**
	 * Empty reason code.
	 */
	protected static final Optional<String> EMPTY_REASON = Optional.empty();

	/**
	 * Reason code for an immutable set of contents.
	 */
	private static final Optional<String> CANNOT_REMOVE = Optional.of("contents.remove.immutable");

	/**
	 * Default placement key for objects in this set of contents.
	 */
	public static final String PLACEMENT_DEFAULT = "in";

	/**
	 * Policy for enumerating this set of contents.
	 */
	public enum EnumerationPolicy {
		/**
		 * All contents are enumerated.
		 */
		DEFAULT,

		/**
		 * Only perceived contents are enumerated.
		 */
		PERCEIVED,

		/**
		 * Contents cannot be enumerated.
		 */
		NONE,

		/**
		 * Contents are currently not available for enumeration, e.g. a closed container.
		 */
		CLOSED
	}

	private final List<Thing> contents = new ArrayList<>();

	/**
	 * @return Size of this set of contents
	 */
	public int size() {
		return contents.size();
	}

	/**
	 * @return Whether this set of contents is empty
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * @return Total weight of this set of contents
	 */
	public int weight() {
		return 0;
	}

	/**
	 * @return Placement key for objects in this set of contents
	 * @see #PLACEMENT_DEFAULT
	 */
	public String placement() {
		return PLACEMENT_DEFAULT;
	}

	/**
	 * @return Enumeration policy for this set of contents (default is {@link EnumerationPolicy#DEFAULT})
	 */
	public EnumerationPolicy policy() {
		return EnumerationPolicy.DEFAULT;
	}

	/**
	 * @param thing Thing
	 * @return Whether this set of contents contains the given thing
	 */
	public final boolean contains(Thing thing) {
		return contents.contains(thing);
	}

	/**
	 * @return Contents
	 */
	public Stream<? extends Thing> stream() {
		return contents.stream();
	}

	/**
	 * Helper - Selects contents of this given class.
	 * @param clazz Class
	 * @return Selected contents
	 */
	public final <T extends Thing> Stream<T> select(Class<T> clazz) {
		return StreamUtil.select(clazz, contents.stream());
	}

	/**
	 * Tests whether the given thing can be added to this set of contents.
	 * @param thing Thing to be added
	 * @return Reason code if the given thing cannot be added (default is empty)
	 * @see #isRemoveAllowed()
	 */
	public Optional<String> reason(Thing thing) {
		if(!thing.parent().contents().isRemoveAllowed()) return CANNOT_REMOVE;
		return EMPTY_REASON;
	}

	/**
	 * @return Whether objects can be removed from this set of contents (default is <tt>true</tt>)
	 */
	public boolean isRemoveAllowed() {
		return true;
	}

	/**
	 * Adds an object to this set of contents.
	 * <p>
	 * Note that this method does <b>not</b> enforce any constraints on this set of contents specified by {@link #reason(Thing)}.
	 * i.e. the owner of the contents is responsible for ensuring that invalid objects are not added.
	 * @param thing Thing to add
	 */
	protected <T extends Thing> void add(T thing) {
		contents.add(thing);
	}

	/**
	 * Removes the given object from this set of contents.
	 * @param thing Thing to remove
	 */
	protected void remove(Thing thing) {
		assert contents.contains(thing);
		contents.remove(thing);
	}

	/**
	 * Updates this set of contents due to a modification to one or more of its children.
	 * @throws UnsupportedOperationException by default
	 * @see #MODIFIED
	 * @see Parent#notify(ContentsNotification)
	 */
	protected void update() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Moves this set of contents to the given parent.
	 * @param parent Parent
	 */
	public void move(Parent parent) {
		new ArrayList<>(contents).forEach(t -> t.parent(parent));
		assert contents.isEmpty();
	}

	/**
	 * Destroys all objects in this set of contents.
	 */
	public void destroy() {
		new ArrayList<>(contents).forEach(Thing::destroy);
		assert contents.isEmpty();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("policy", policy())
			.append("size", size())
			.toString();
	}
}
