package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class LootFactoryTest {
	@Test
	public void none() {
		assertEquals(0, LootFactory.EMPTY.generate(null).count());
	}

	@Test
	public void descriptor() {
		// Create factory
		final ObjectDescriptor descriptor = ObjectDescriptor.of("object");
		final LootFactory factory = LootFactory.of(descriptor, 1);
		assertNotNull(factory);

		// Generate loot
		final Stream<WorldObject> loot = factory.generate(null);
		assertNotNull(loot);

		// Check loot
		final Collection<WorldObject> results = loot.collect(toList());
		assertEquals(1, results.size());
		assertEquals(descriptor, results.iterator().next().descriptor());
	}

	@Test
	public void money() {
		// Create factory
		final var factory = LootFactory.money(42);
		assertNotNull(factory);

		// Check loot
		final var loot = factory.generate(null);
		assertNotNull(loot);

		// Check results
		final var results = loot.collect(toList());
		assertEquals(1, results.size());

		// Check money
		final var money = (Money) results.iterator().next();
		assertEquals(42, money.value());
	}
}
