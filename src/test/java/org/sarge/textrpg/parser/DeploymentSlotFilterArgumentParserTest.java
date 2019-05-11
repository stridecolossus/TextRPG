package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class DeploymentSlotFilterArgumentParserTest {
	private DeploymentSlotFilterArgumentParser parser;

	@BeforeEach
	public void before() {
		parser = new DeploymentSlotFilterArgumentParser();
	}

	@Test
	public void parse() {
		// Create name-store
		final NameStore store = mock(NameStore.class);
		when(store.matches("slot.worn", "worn")).thenReturn(true);
		when(store.matches("slot.head", "head")).thenReturn(true);

		// Parse filter
		final WordCursor cursor = new WordCursor("worn head", store, Set.of());
		final ObjectDescriptor.Filter filter = parser.parse(cursor);
		assertNotNull(filter);

		// Check filter
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("object").slot(Slot.HEAD).build();
		assertEquals(true, filter.test(descriptor));
		assertEquals(false, filter.test(ObjectDescriptor.of("invalid")));
	}
}
