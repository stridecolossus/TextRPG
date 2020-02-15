package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;

public class IntangibleTest {
	private Intangible intangible;

	@BeforeEach
	public void before() {
		intangible = new Intangible("name", Percentile.HALF, Map.of(Emission.LIGHT, Percentile.ONE));
	}

	@Test
	public void constructor() {
		assertEquals("name", intangible.name());
		assertEquals(Percentile.HALF, intangible.visibility());
		assertEquals(Percentile.ONE, intangible.emission(Emission.LIGHT));
		assertEquals(0, intangible.weight());
		assertEquals(Size.NONE, intangible.size());
	}

	@Test
	public void describe() {
		final var expected = new Description("intangible.object", "name");
		assertEquals(expected, intangible.describe());
	}
}
