package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;

public class ListSkillsActionTest extends ActionTestBase {
	private ListSkillsAction action;

	@BeforeEach
	public void before() {
		action = new ListSkillsAction();
	}

	@Test
	public void skills() {
		addRequiredSkill();
		final Response response = action.skills(actor);
		final Response expected = new Response.Builder().add("list.skills.header").add("skill.skill").build();
		assertEquals(expected, response);
	}

	@Test
	public void skillsNone() {
		final Response response = action.skills(actor);
		assertEquals(Response.of("list.skills.none"), response);
	}
}
