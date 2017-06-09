package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.Race.Builder;
import org.sarge.textrpg.entity.Skill.Tier;
import org.sarge.textrpg.object.LootFactory;

public class RaceTest {
	private Race race;
	private Skill skill;
	
	@Before
	public void before() {
		skill = new Skill("skill", Collections.singletonList(new Tier(Condition.TRUE, 1)));
		race = new Builder("race")
			.gender(Gender.FEMALE)
			.alignment(Alignment.EVIL)
			.size(Size.HUGE)
			.attribute(Attribute.AGILITY, 2)
			.skills(new SkillSet(skill, 3))
			.butcherFactory(mock(LootFactory.class))
			.mount()
			.build();
	}
	
	@Test
	public void constructor() {
		// Race
		assertEquals("race", race.getName());
		
		// Attributes
		assertNotNull(race.getAttributes());
		assertEquals(Gender.FEMALE, race.getAttributes().getDefaultGender());
		assertEquals(Alignment.EVIL, race.getAttributes().getDefaultAlignment());
		assertEquals(Size.HUGE, race.getAttributes().getSize());
		assertNotNull(race.getAttributes().getAttributes());
		assertEquals(2, race.getAttributes().getAttributes().get(Attribute.AGILITY));
		assertEquals(true, race.getAttributes().isMount());
		
		// Gear and skills
		assertNotNull(race.getEquipment());
		assertNotNull(race.getEquipment().getSkills());
		assertEquals(1, race.getEquipment().getSkills().getSkills().count());
		assertEquals(Optional.of(3), race.getEquipment().getSkills().getLevel(skill));
		
		// Kill descriptor
		assertNotNull(race.getKillDescriptor());
		assertEquals(true, race.getKillDescriptor().isCorporeal());
		assertNotNull(race.getKillDescriptor().getButcherFactory());
		assertEquals(true, race.getKillDescriptor().getButcherFactory().isPresent());
	}
}
