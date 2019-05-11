package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.contents.TrackedContents;

/**
 * Container for {@link Ammo}.
 * @author Sarge
 */
public class Quiver extends WorldObject implements Parent {
	private static final Optional<String> INCORRECT_TYPE = Optional.of("quiver.invalid.object");
	private static final Optional<String> INCORRECT_AMMO = Optional.of("quiver.invalid.ammo");
	private static final Optional<String> QUIVER_FULL = Optional.of("quiver.full");

	/**
	 * Quiver descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Ammo.Type type;
		private final int max;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param type				Ammo type
		 * @param max				Maximum total size of contents
		 */
		public Descriptor(ObjectDescriptor descriptor, Ammo.Type type, int max) {
			super(descriptor);
			this.type = notNull(type);
			this.max = oneOrMore(max);
		}

		/**
		 * @return Ammo type of this quiver
		 */
		public Ammo.Type type() {
			return type;
		}

		@Override
		public WorldObject create() {
			return new Quiver(this);
		}
	}

	// TODO - Contents needs to be an INTERFACE!?

	/**
	 * Quiver contents.
	 */
	private class QuiverContents extends TrackedContents {
		private final Map<Ammo.Descriptor, ObjectStack> stacks = new HashMap<>();
		private int count;
		private int weight;

		@Override
		public int size() {
			return stacks.size();
		}

		@Override
		public int weight() {
			return weight;
		}

		@Override
		public Stream<? extends Thing> stream() {
			return stacks.values().stream();
		}

		@Override
		public Optional<String> reason(Thing thing) {
			if(thing instanceof Ammo) {
				final Descriptor quiver = descriptor();
				final Ammo ammo = (Ammo) thing;
				if(count + ammo.count() >= quiver.max) {
					return QUIVER_FULL;
				}
				else
				if(ammo.descriptor().type() == quiver.type()) {
					return Contents.EMPTY_REASON;
				}
				else {
					return INCORRECT_AMMO;
				}
			}
			else {
				return INCORRECT_TYPE;
			}
		}

		@Override
		protected void add(Thing thing) {
			// Add to stack
			final Ammo ammo = (Ammo) thing;
			final Ammo.Descriptor descriptor = ammo.descriptor();
			stacks.computeIfAbsent(descriptor, ignore -> new ObjectStack(descriptor, 0)).modify(ammo.count());

			// Update stats and iterator
			++count;
			weight += ammo.weight();

			// Consume
			ammo.destroy();
		}

		@Override
		protected void remove(Thing thing) {
			// TODO - used?
			//super.remove(thing);
		}

		/**
		 * Selects the next ammo.
		 * @return Next ammo
		 */
		private Ammo.Descriptor next() {
			if(stacks.isEmpty()) {
				return null;
			}
			else {
				// TODO - select by value
				return stacks.keySet().iterator().next();
			}
		}
	}

	/**
	 * Ammo iterator.
	 */
	private class AmmoIterator implements Iterator<Ammo.Descriptor> {
		private Ammo.Descriptor next;

		@Override
		public boolean hasNext() {
			if(next == null) {
				next = contents.next();
				return next != null;
			}
			else {
				return true;
			}
		}

		@Override
		public Ammo.Descriptor next() {
			if(next == null) throw new NoSuchElementException();

			// Note next ammo
			final Ammo.Descriptor result = next;

			// Consume ammo
			final ObjectStack stack = contents.stacks.get(next);
			stack.modify(-1);

			// Remove stack if consumed
			if(stack.count() == 0) {
				contents.stacks.remove(next);
				next = contents.next();
			}

			return result;
		}
	}

	private final QuiverContents contents = new QuiverContents();
	private final AmmoIterator iterator = new AmmoIterator();

	/**
	 * Constructor.
	 * @param descriptor Quiver descriptor
	 */
	protected Quiver(Descriptor descriptor) {
		super(descriptor);
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
	public int weight() {
		return super.weight() + contents.weight();
	}

	/**
	 * @return Ammo iterator
	 */
	public Iterator<Ammo.Descriptor> iterator() {
		return iterator;
	}
}
