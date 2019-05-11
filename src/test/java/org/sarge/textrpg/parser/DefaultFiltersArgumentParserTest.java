package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.Food;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class DefaultFiltersArgumentParserTest {
	private ArgumentParser<ObjectDescriptor.Filter> parser;
	private NameStore store;

	@BeforeEach
	public void before() {
		parser = new DefaultFiltersArgumentParser();
		store = mock(NameStore.class);
		when(store.matches("all", "all")).thenReturn(true);
		when(store.matches("edible", "edible")).thenReturn(true);
		when(store.matches("containers", "containers")).thenReturn(true);
	}

	private void run(String word, ObjectDescriptor descriptor) {
		final WordCursor cursor = new WordCursor(word, store, Set.of());
		final ObjectDescriptor.Filter filter = parser.parse(cursor);
		assertNotNull(filter);
		assertTrue(filter.test(descriptor));
	}

	@Test
	public void filters() {
		run("all", mock(ObjectDescriptor.class));
		run("edible", mock(Food.Descriptor.class));
		run("containers", mock(Container.Descriptor.class));
	}

	@Test
	public void invalid() {
		final WordCursor cursor = new WordCursor("cobblers", store, Set.of());
		assertEquals(null, parser.parse(cursor));
	}
}
