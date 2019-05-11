package org.sarge.textrpg.contents;

import java.util.List;
import java.util.Optional;

/**
 * Set of contents that has limited mutability, e.g. a corpse or a bookshelf with a fixed set of books.
 * <p>
 * Objects cannot be be added to an immutable set of contents, i.e. {@link #reason(Thing)} <b>always</b> returns a reason code.
 * <p>
 * An immutable set of contents can also optionally prevent any contents from being removed, see {@link #ImmutableContents(boolean)}.
 * @author Sarge
 */
public class ImmutableContents extends TrackedContents {
	private static final Optional<String> IMMUTABLE = Optional.of("contents.add.immutable");

	private final boolean remove;

	/**
	 * Constructor.
	 * @param remove Whether objects can be removed from this set of contents
	 * @see #isRemoveAllowed()
	 */
	public ImmutableContents(boolean remove, List<? extends Thing> contents) {
		this.remove = remove;
		contents.forEach(super::add);
		update();
	}

	@Override
	public boolean isRemoveAllowed() {
		return remove;
	}

	@Override
	public Optional<String> reason(Thing thing) {
		return IMMUTABLE;
	}

	@Override
	protected void remove(Thing thing) {
		assert remove;
		super.remove(thing);
	}
}
