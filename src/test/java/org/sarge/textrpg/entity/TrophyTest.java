package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;

public class TrophyTest {
	private Trophy trophy;
	private Race race;

	@BeforeEach
	public void before() {
		trophy = new Trophy();
		race = new Race.Builder("race").build();
	}

	@Test
	public void add() {
		trophy.add(race);
		trophy.add(race);
		assertEquals(2, trophy.count(race));
	}

	@Test
	public void describe() {
		trophy.add(race);
		final var description = trophy.describe();
		final var expected = new Description.Builder("trophy.entry").name("race").add("count", 1).build();
		assertEquals(List.of(expected), description);
	}
}
