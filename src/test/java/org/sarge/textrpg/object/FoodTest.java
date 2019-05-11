package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.TestHelper;

public class FoodTest {
	private Food food;

	@BeforeEach
	public void before() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("meat").decay(Duration.ofMinutes(1)).build();
		food = new Food.Descriptor(descriptor, true, 2, 3f).create();
		food.parent(TestHelper.parent());
	}

	@Test
	public void constructor() {
		assertEquals("meat", food.name());
		assertNotNull(food.descriptor());
		assertEquals(true, food.isCookable());
		assertEquals(2, food.descriptor().nutrition());
		assertEquals(false, food.descriptor().isStackable());
	}

	@Test
	public void cook() {
		final Food cooked = food.cook();
		assertNotNull(cooked);
		assertEquals(false, cooked.isCookable());
		assertEquals(2 * 3, cooked.descriptor().nutrition());
	}

	@Test
	public void cookAlreadyCooked() {
		final Food cooked = food.cook();
		assertThrows(IllegalStateException.class, () -> cooked.cook());
	}

	@DisplayName("Decay to rotten meat")
	@Test
	public void decayMeat() {
		assertEquals(true, food.decay());
		assertEquals(true, food.isAlive());
		assertEquals(false, food.isCookable());
		assertEquals("rotten.meat", food.name());
	}

	@DisplayName("Decay destroys rotten meat")
	@Test
	public void decayRotten() {
		assertEquals(true, food.decay());
		assertEquals(false, food.decay());
		assertEquals(false, food.isAlive());
	}
}
