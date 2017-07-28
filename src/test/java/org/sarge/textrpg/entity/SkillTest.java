package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.entity.Skill.Tier;

public class SkillTest {
	private Skill skill;
	
	@Before
	public void before() {
		skill = new Skill("skill", Collections.singletonList(new Tier(Condition.TRUE, 1)));
	}
	
	@Test
	public void constructor() {
		assertEquals("skill", skill.getName());
		assertEquals(1, skill.getMaximum());
	}
	
	@Test
	public void getTier() {
		final Tier tier = skill.getTier(0);
		assertNotNull(tier);
		assertEquals(Condition.TRUE, tier.getCondition());
		assertEquals(1, tier.getCost());
	}
}
