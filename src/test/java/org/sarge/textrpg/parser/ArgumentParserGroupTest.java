package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ArgumentParser;

public class ArgumentParserGroupTest {
	private ArgumentParser<?> parser;
	private ArgumentParserGroup group;

	@BeforeEach
	public void before() {
		parser = mock(ArgumentParser.class);
		group = new ArgumentParserGroup();
	}

	@Test
	public void iteratorEmpty() {
		assertThrows(IllegalStateException.class, () -> group.iterator(Object.class));
	}

	@Test
	public void iterator() {
		// Create a registry
		final ArgumentParser.Registry registry = type -> List.of(parser, parser);
		group.add(() -> registry);

		// Check iterator
		final Iterator<ArgumentParser<?>> iterator = group.iterator(Object.class);
		assertNotNull(iterator);
		assertEquals(parser, iterator.next());
		assertEquals(parser, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	public void iteratorMultipleRegistry() {
		// Create a group for two parsers
		final ArgumentParser.Registry registry = type -> List.of(parser);
		group.add(() -> registry);
		group.add(() -> registry);

		// Check iterator returns parser from each registry
		final Iterator<ArgumentParser<?>> iterator = group.iterator(Object.class);
		assertNotNull(iterator);
		assertEquals(parser, iterator.next());
		assertEquals(parser, iterator.next());
		assertEquals(false, iterator.hasNext());
	}
}
