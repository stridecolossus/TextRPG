package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.entity.CalculationLoader;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.Percentile;

public class SkillLoaderTest {
	private SkillLoader loader;
	private CalculationLoader calc;

	@BeforeEach
	public void before() {
		calc = mock(CalculationLoader.class);
		loader = new SkillLoader(calc);
	}

	@Test
	public void load() {
		// Create XML
		final Element mod = Element.of("mod");
		final Element xml = new Element.Builder("skill")
			.attribute("name", "name")
			.attribute("power", 1)
			.attribute("score", 2)
			.attribute("default", 3)
			.child("modifier")
				.add(mod)
				.end()
			.attribute("duration", "5s")
			.attribute("scale", 6)
			.attribute("cost", 7)
			.build();

		// Init calculation loader
		when(calc.load(mod)).thenReturn(mock(Calculation.class));

		// Load skill
		final Skill skill = loader.load(xml);
		assertNotNull(skill);
		assertEquals(1, skill.power());
		assertEquals(Percentile.of(2), skill.score());
		assertEquals(Duration.ofSeconds(5), skill.duration());
		assertEquals(6, skill.scale());
		assertEquals(7, skill.cost());
	}
}
