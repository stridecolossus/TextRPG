package org.sarge.textrpg.common;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.SkillSet.MutableSkillSet;

public class SkillSetTest {
	private MutableSkillSet set;
	private Skill one, two, req;

	@BeforeEach
	public void before() {
		set = new MutableSkillSet();
		req = new Skill.Builder().name("req").build();
		one = new Skill.Builder().name("one").required(req).build();
		two = new Skill.Builder().name("two").previous(one).build();
	}

	@Test
	public void constructor() {
		assertNotNull(set.stream());
		assertEquals(0, set.stream().count());
		assertEquals(false, set.contains(one));
	}

	@Test
	public void findInitialSkill() {
		set.add(req);
		set.add(one);
		assertEquals(one, set.find(one));
		assertEquals(req, set.find(req));
	}

	@Test
	public void findAdvancedSkill() {
		set.add(req);
		set.add(one);
		set.add(two);
		assertEquals(two, set.find(one));
		assertEquals(two, set.find(two));
		assertEquals(req, set.find(req));
	}

	@Test
	public void findNotPresent() {
		assertThrows(IllegalArgumentException.class, () -> set.find(req));
		assertThrows(IllegalArgumentException.class, () -> set.find(one));
		assertThrows(IllegalArgumentException.class, () -> set.find(two));
	}

	@Test
	public void validate() {
		assertEquals(Set.of(), set.validate(req));
		assertEquals(Set.of(req), set.validate(one));
		assertEquals(Set.of(one, req), set.validate(two));
	}

	@Test
	public void add() {
		set.add(req);
		set.add(one);
		set.add(two);
		assertEquals(true, set.contains(req));
		assertEquals(true, set.contains(one));
		assertEquals(true, set.contains(two));
		assertEquals(Set.of(req, one, two), set.stream().collect(toSet()));
	}

	@Test
	public void addDuplicate() {
		set.add(req);
		assertThrows(IllegalArgumentException.class, () -> set.add(req));
	}

	@Test
	public void addMissingPreviousSkill() {
		set.add(req);
		assertThrows(IllegalStateException.class, () -> set.add(two));
	}

	@Test
	public void addMissingRequiredSkill() {
		assertThrows(IllegalStateException.class, () -> set.add(one));
	}
}
