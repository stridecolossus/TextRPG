package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.SkillSet.MutableSkillSet;
import org.sarge.textrpg.entity.CharacterEntity.CharacterEntityDescriptor;

public class CharacterEntityTest {
	private CharacterEntity entity;
	private Topic topic;
	private MovementMode movement;

	@BeforeEach
	public void before() {
		topic = new Topic("topic");
		final Race race = new Race.Builder("race").build();
		final EntityDescriptor descriptor = new CharacterEntityDescriptor(race, "name", Gender.FEMALE, Alignment.EVIL, null, new MutableSkillSet(), Set.of(topic));
		entity = new CharacterEntity(descriptor, mock(EntityManager.class)); //new EntityManager(mock(Event), mock(Notification.Handler.class), () -> false));
		movement = mock(MovementMode.class);
	}

	@Test
	public void constructor() {
		assertEquals("name", entity.name());
		assertEquals(Gender.FEMALE, entity.descriptor().gender());
		assertEquals(Alignment.EVIL, entity.descriptor().alignment());
		assertNotNull(entity.skills());
		assertNotNull(entity.descriptor().topics());
		assertArrayEquals(new Topic[]{topic}, entity.descriptor().topics().toArray());
		assertNotNull(entity.movement());
		assertTrue(entity.movement() instanceof Entity.DefaultMovementMode);
		assertNotNull(entity.follower());
	}

	@Test
	public void movement() {
		entity.movement(movement);
		assertEquals(movement, entity.movement());
	}

	@Test
	public void movementClear() {
		final MovementMode prev = entity.movement();
		entity.movement(movement);
		entity.movement(null);
		assertEquals(prev, entity.movement());
	}
}
