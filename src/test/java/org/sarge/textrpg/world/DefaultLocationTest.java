package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultLocationTest {
	private DefaultLocation.Builder builder;

	@BeforeEach
	public void before() {
		builder = new DefaultLocation.Builder().descriptor(new Location.Descriptor("loc"));
	}

	@Test
	public void build() {
		final DefaultLocation loc = builder.build();
		assertNotNull(loc);
		assertEquals("loc", loc.name());
		assertEquals(Area.ROOT, loc.area());
	}

	@Test
	public void buildAlreadyBuilt() {
		builder.build();
		assertThrows(IllegalStateException.class, () -> builder.build());
	}

	@Test
	public void addExitOrphan() {
		final DefaultLocation loc = builder.orphan().build();
		assertThrows(IllegalArgumentException.class, () -> loc.exits.add(new Exit(Direction.EAST, Link.DEFAULT, loc)));
	}
}
