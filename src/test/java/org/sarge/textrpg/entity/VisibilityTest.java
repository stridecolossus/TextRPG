package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Percentile;

public class VisibilityTest {
	private Visibility vis;

	@BeforeEach
	public void before() {
		vis = new Visibility();
	}

	@Test
	public void constructor() {
		assertEquals(Percentile.ONE, vis.get());
	}

	@Test
	public void stance() {
		vis.stance(Percentile.HALF);
		assertEquals(Percentile.HALF, vis.get());
	}

	@Test
	public void stanceAlreadyApplied() {
		vis.stance(Percentile.HALF);
		assertThrows(IllegalStateException.class, () -> vis.stance(Percentile.HALF));
	}

	@Test
	public void remove() {
		vis.stance(Percentile.HALF);
		vis.remove();
		assertEquals(Percentile.ONE, vis.get());
	}

	@Test
	public void removeNotApplied() {
		assertThrows(IllegalStateException.class, () -> vis.remove());
	}

	@Test
	public void modifier() {
		vis.modifier(25);
		vis.modifier(50);
		assertEquals(Percentile.HALF, vis.get());
	}
}
