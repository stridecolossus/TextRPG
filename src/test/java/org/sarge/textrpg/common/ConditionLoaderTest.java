package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.Registry;

public class ConditionLoaderTest {
	private ConditionLoader loader;

	@BeforeEach
	public void before() {
		@SuppressWarnings("unchecked")
		final Registry<Skill> skills = mock(Registry.class);
		when(skills.get("skill")).thenReturn(Skill.NONE);
		loader = new ConditionLoader(skills);
	}

	@Test
	public void loadSkill() {
		final Element xml = new Element.Builder("skill").attribute("skill", "skill").build();
		assertNotNull(loader.load(xml));
	}

	@Test
	public void loadRace() {
		final Element xml = new Element.Builder("race").attribute("cat", "cat").build();
		assertNotNull(loader.load(xml));
	}

	@Test
	public void loadAttribute() {
		final Element xml = new Element.Builder("attribute").attribute("attribute", "agility").attribute("min", 42).build();
		assertNotNull(loader.load(xml));
	}
}
