package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.world.Faction;

public class DefaultEntityDescriptorTest {
	private EntityDescriptor descriptor;
	private Race race;

	@BeforeEach
	public void before() {
		race = new Race.Builder("race").gender(Gender.FEMALE).alignment(Alignment.EVIL).skill(Skill.NONE).build();
		descriptor = new DefaultEntityDescriptor(race, mock(Faction.class));
	}

	@Test
	public void constructor() {
		assertEquals(race, descriptor.race());
		assertEquals("race", descriptor.name());
		assertNotNull(descriptor.attributes());
		assertEquals(Gender.FEMALE, descriptor.gender());
		assertEquals(Alignment.EVIL, descriptor.alignment());
		assertNotNull(descriptor.faction());
		assertTrue(descriptor.faction().isPresent());
		assertNotNull(descriptor.skills());
		assertNotNull(descriptor.topics());
		assertEquals(0, descriptor.topics().count());
	}
}
