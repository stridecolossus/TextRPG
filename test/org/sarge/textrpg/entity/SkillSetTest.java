package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Skill.Tier;

public class SkillSetTest {
	private SkillSet skills;
	private Skill skill;
	
	@Before
	public void before() {
		skills = new SkillSet();
		skill = new Skill("skill", Collections.singletonList(new Tier(Condition.TRUE, 1)));
	}
	
	@Test
	public void constructor() {
		assertEquals(0, skills.getSkills().count());
		assertEquals(Optional.empty(), skills.getLevel(skill));
	}
	
	@Test
	public void increment() {
		skills.increment(skill, 1);
		assertEquals(1, skills.getSkills().count());
		assertEquals(skill, skills.getSkills().iterator().next());
		assertEquals(Optional.of(1), skills.getLevel(skill));
	}
	
	@Test
	public void describe() {
		skills.increment(skill, 1);
		final List<Description> desc = skills.describe();
		assertNotNull(desc);
		assertEquals(1, desc.size());
	}
}
