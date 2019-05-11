package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.object.ObjectDescriptor;

public class FurnitureTest {
	private Furniture furniture;

	@BeforeEach
	public void before() {
		final var descriptor = new ObjectDescriptor.Builder("chair").size(Size.SMALL).build();
		furniture = new Furniture(new Furniture.Descriptor(descriptor, Set.of(Stance.RESTING), 1, "in"));
	}

	@Test
	public void constructor() {
		assertEquals("chair", furniture.name());
		assertNotNull(furniture.contents());
		assertEquals(true, furniture.contents().isEmpty());
	}

	@Test
	public void isValid() {
		assertEquals(true, furniture.descriptor().isValid(Stance.RESTING));
		assertEquals(false, furniture.descriptor().isValid(Stance.SLEEPING));
	}
}
