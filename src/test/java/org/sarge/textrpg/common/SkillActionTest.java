package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.ActionException;

public class SkillActionTest extends ActionTestBase {
	private SkillAction action;

	@BeforeEach
	public void before() {
		action = new SkillAction(skill) {
			// Mock implementation
		};
	}

	@Test
	public void verify() throws ActionException {
		action.verify(actor);
	}

	@Test
	public void verifyWithSkill() throws ActionException {
		addRequiredSkill();
		action.verify(actor);
	}

	@Test
	public void power() {
		assertEquals(42, action.power(actor));
	}

	@Test
	public void skill() {
		addRequiredSkill();
		assertEquals(skill, action.skill(actor));
	}

	@Test
	public void defaultSkill() {
		assertEquals(skill, action.skill(actor));
	}

	@Test
	public void skillMissingMandatory() {
		final Skill mandatory = new Skill.Builder().name("mandatory").build();
		action = new SkillAction(mandatory) {
			// Mock implementation
		};
		assertThrows(IllegalStateException.class, () -> action.skill(actor));
	}
}
