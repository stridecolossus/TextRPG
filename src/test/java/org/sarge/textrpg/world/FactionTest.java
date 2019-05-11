package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.PeriodModel;

public class FactionTest {
	private Faction faction;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		faction = new Faction("name", Alignment.EVIL, Area.ROOT, mock(PeriodModel.class), mock(Calendar.class));
	}

	@Test
	public void constructor() {
		assertEquals("name", faction.name());
		assertEquals(Alignment.EVIL, faction.alignment());
		assertNotNull(faction.opening());
		assertNotNull(faction.calendar());
	}

	@Test
	public void level() {
		assertEquals(Relationship.FRIENDLY, faction.relationship(Alignment.EVIL));
		assertEquals(Relationship.NEUTRAL, faction.relationship(Alignment.NEUTRAL));
		assertEquals(Relationship.ENEMY, faction.relationship(Alignment.GOOD));
	}

	@Test
	public void Relationship() {
		final Faction.Association association = new Faction.Association(faction, Relationship.FRIENDLY);
		assertEquals(faction, association.faction());
		assertEquals(Relationship.FRIENDLY, association.relationship());
		assertEquals(association, association);
		assertNotEquals(association, new Faction.Association(faction, Relationship.NEUTRAL));
		assertNotEquals(association, null);
	}
}
