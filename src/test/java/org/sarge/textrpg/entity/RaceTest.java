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
		assertEquals("race", race.name());
		
		// Attributes
		assertNotNull(race.attributes());
		assertEquals(Gender.FEMALE, race.attributes().gender());
		assertEquals(Alignment.EVIL, race.attributes().alignment());
		assertEquals(Size.HUGE, race.attributes().size());
		assertNotNull(race.attributes().attributes());
		assertEquals(2, race.attributes().attributes().get(Attribute.AGILITY));
		assertEquals(true, race.attributes().isMount());
		
		// Gear and skills
		assertNotNull(race.equipment());
		assertNotNull(race.equipment().skills());
		assertEquals(1, race.equipment().skills().getSkills().count());
		assertEquals(Optional.of(3), race.equipment().skills().getLevel(skill));
		
		// Kill descriptor
		assertNotNull(race.killDescriptor());
		assertEquals(true, race.killDescriptor().isCorporeal());
		assertNotNull(race.killDescriptor().butcherFactory());
		assertEquals(true, race.killDescriptor().butcherFactory().isPresent());
	}
}
