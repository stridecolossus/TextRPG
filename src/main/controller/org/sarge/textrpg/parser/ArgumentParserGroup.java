package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ArgumentParser;

/**
 * Iterator over a group of argument parsers.
 * @author Sarge
 */
public class ArgumentParserGroup {
	private final List<Supplier<ArgumentParser.Registry>> group = new StrictList<>();

	/**
	 * Adds a registry.
	 * @param registry Registry
	 */
	public void add(Supplier<ArgumentParser.Registry> registry) {
		Check.notNull(registry);
		group.add(registry);
	}

	/**
	 * Creates an iterator over the parsers in this group for the given type.
	 * @param type Type
	 * @return Parser iterator
	 */
	public Iterator<ArgumentParser<?>> iterator(Class<?> type) {
		Check.notNull(type);
		if(group.isEmpty()) throw new IllegalStateException("Parser group is empty");
		return new GroupIterator(type);
	}

	/**
	 * Iterator implementation.
	 */
	private class GroupIterator implements Iterator<ArgumentParser<?>> {
		private final Iterator<Supplier<ArgumentParser.Registry>> itr = group.iterator();
		private final Class<?> type;

		private Iterator<ArgumentParser<?>> current;

		/**
		 * Constructor.
		 * @param type Type filter
		 */
		private GroupIterator(Class<?> type) {
			this.type = notNull(type);
			update();
		}

		@Override
		public boolean hasNext() {
			return current.hasNext();
		}

		@Override
		public ArgumentParser<?> next() {
			if(!current.hasNext()) {
				update();
			}
			return current.next();
		}

		/**
		 * Updates this iterator to the next entry in the group.
		 */
		private void update() {
			while(itr.hasNext()) {
				current = itr.next().get().parsers(type).iterator();
				if(current.hasNext()) {
					return;
				}
			}
		}
	}
}
