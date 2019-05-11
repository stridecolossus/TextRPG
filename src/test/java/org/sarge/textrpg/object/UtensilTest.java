package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.TestHelper;

public class UtensilTest {
	private Utensil utensil;

	@BeforeEach
	public void before() {
		final var descriptor = new ObjectDescriptor.Builder("utensil").weight(1).size(Size.SMALL).build();
		utensil = new Utensil(new Utensil.Descriptor(descriptor, 1));
	}

	@Test
	public void constructor() {
		assertEquals("utensil", utensil.name());
		assertEquals(1, utensil.weight());
		assertEquals(false, utensil.isWater());
		assertNotNull(utensil.contents());
		assertEquals(true, utensil.contents().isEmpty());
	}

	@Test
	public void water() {
		utensil.water(true);
		assertEquals(true, utensil.isWater());
	}

	@Test
	public void waterEmpty() {
		utensil.water(false);
		assertEquals(false, utensil.isWater());
	}

	private static Food food() {
		final var descriptor = new ObjectDescriptor.Builder("ingredient").weight(2).decay(Duration.ofMillis(1)).build();
		final Food food = new Food.Descriptor(descriptor, false, 2, 3f).create();
		food.parent(TestHelper.parent());
		return food;
	}

	@Test
	public void add() {
		final var food = food();
		assertEquals(Optional.empty(), utensil.contents().reason(food));
		food.parent(utensil);
		assertEquals(1, utensil.contents().size());
		assertEquals(1 + 2, utensil.weight());
	}

	@Test
	public void addCapacity() {
		final var food = food();
		final var more = food();
		food.parent(utensil);
		assertEquals(Optional.of("utensil.limit.capacity"), utensil.contents().reason(more));
	}

	@Test
	public void addCooked() {
		final var cooked = food().cook();
		assertEquals(Optional.of("utensil.limit.ingredient"), utensil.contents().reason(cooked));
	}

	@Test
	public void addInvalid() {
		final var obj = ObjectDescriptor.of("invalid").create();
		assertEquals(Optional.of("utensil.limit.ingredient"), utensil.contents().reason(obj));
	}
}
