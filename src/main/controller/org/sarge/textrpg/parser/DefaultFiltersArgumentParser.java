package org.sarge.textrpg.parser;

import java.util.List;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.Food;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Filter;
import org.sarge.textrpg.util.WordCursor;
import org.springframework.stereotype.Component;

/**
 * Argument parser for default object filters.
 * @author Sarge
 */
@Component
public class DefaultFiltersArgumentParser implements ArgumentParser<ObjectDescriptor.Filter> {
	/**
	 * Wrapper for an object-descriptor filter as a command-argument.
	 */
	private static class Entry implements CommandArgument {
		private final String name;
		private final ObjectDescriptor.Filter filter;

		/**
		 * Constructor.
		 * @param name			Filter name
		 * @param filter		Filter
		 */
		private Entry(String name, Filter filter) {
			this.name = name;
			this.filter = filter;
		}

		@Override
		public String name() {
			return name;
		}
	}

	/**
	 * Filter command arguments.
	 */
	private static final List<Entry> FILTERS = List.of(
		new Entry("all", ObjectDescriptor.Filter.ALL),
		new Entry("edible", Food.Descriptor.FILTER),
		new Entry("containers", Container.Descriptor.FILTER)
	);

	@Override
	public ObjectDescriptor.Filter parse(WordCursor cursor) {
		final Entry result = ArgumentParser.matches(cursor, FILTERS.stream());
		if(result == null) {
			return null;
		}
		else {
			return result.filter;
		}
	}
}
