package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.Percentile;

public class SkillTest {
	private Skill advanced, skill, required;

	@BeforeEach
	public void before() {
		// Create first optional skill
		skill = new Skill.Builder()
			.name("skill")
			.power(1)
			.score(Percentile.of(2))
			.defaultScore(Percentile.of(3))
			.modifier(Calculation.ZERO)
			.duration(Duration.ofSeconds(4))
			.scale(5)
			.cost(6)
			.build();

		// Create a required skill
		required = new Skill.Builder()
			.name("required")
			.build();

		// Create advanced skill
		advanced = new Skill.Builder()
			.name("advanced")
			.previous(skill)
			.required(required)
			.build();
	}

	@Test
	public void constructor() {
		assertEquals("skill", skill.name());
		assertEquals(false, skill.isMandatory());
		assertEquals(1, skill.power());
		assertEquals(Percentile.of(2), skill.score());
		assertNotNull(skill.modifier());
		assertEquals(Duration.ofSeconds(4), skill.duration());
		assertEquals(5, skill.scale());
		assertEquals(6, skill.cost());
		assertEquals(List.of(), skill.required());
		assertEquals(skill, skill.group());
		assertEquals(Optional.of(advanced), skill.next());
	}

	@Test
	public void defaultSkill() {
		final Skill def = skill.defaultSkill();
		assertNotNull(def);
		assertEquals("skill", def.name());
		assertEquals(Percentile.of(3), def.score());
		assertEquals(0, def.cost());
		assertEquals(Optional.empty(), def.next());
		assertEquals(skill, def.group());
	}

	@Test
	public void advancedSkill() {
		assertEquals(true, advanced.isMandatory());
		assertEquals(skill, advanced.group());
		assertEquals(List.of(required, skill), advanced.required());
		assertEquals(Optional.empty(), advanced.next());
	}

	@Test
	public void invalidPreviousSkill() {
		assertThrows(IllegalArgumentException.class, () -> new Skill.Builder().name("invalid").previous(skill).build());
	}

	@Test
	public void invalidRequiredSkill() {
		assertThrows(IllegalArgumentException.class, () -> new Skill.Builder().name("invalid").previous(advanced).required(advanced).build());
	}
}
