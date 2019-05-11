package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.entity.Race.Behaviour;
import org.sarge.textrpg.entity.Race.Characteristics;
import org.sarge.textrpg.entity.Race.Gear;
import org.sarge.textrpg.entity.Race.Kill;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.MovementManager;

public class RaceTest {
	private Race race;
	private Skill skill;
	private ObjectDescriptor equipment;
	private LootFactory butcher;

	@BeforeEach
	public void before() {
		skill = Skill.NONE;
		equipment = ObjectDescriptor.of("equipment");
		butcher = mock(LootFactory.class);

		race = new Race.Builder("race")
			// Characteristics
			.gender(Gender.FEMALE)
			.alignment(Alignment.EVIL)
			.size(Size.LARGE)
			.weight(1)
			.attribute(Attribute.WILL, 2)
			.body("body")
			.category("cat")
			// Gear
			.equipment(equipment)
			.noise(Percentile.HALF)
			.vocation("vocation")
			.skill(skill)
			.language(skill)
			.tracks(Percentile.HALF)
			// Behaviour
			.aggression(Percentile.HALF)
			.autoflee(Percentile.HALF)
			.behaviour(Behaviour.Flag.STARTLED)
			// Kill descriptor
			.corpse()
			.butcher(butcher)
			.build();
	}

	@Test
	public void constructor() {
		// Check race
		assertNotNull(race);
		assertEquals("race", race.name());

		// Check characteristics
		final Characteristics chars = race.characteristics();
		assertNotNull(chars);
		assertEquals(Gender.FEMALE, chars.gender());
		assertEquals(Alignment.EVIL, chars.alignment());
		assertEquals(Size.LARGE, chars.size());
		assertEquals(1, chars.weight());
		assertNotNull(chars.attributes());
		assertEquals(2, chars.attributes().get(Attribute.WILL).get());
		assertNotNull(chars.body());
		assertArrayEquals(new String[]{"body"}, chars.body().toArray());
		assertTrue(race.characteristics().isCategory("cat"));

		// Check gear
		final Gear gear = race.gear();
		assertNotNull(gear);
		assertNotNull(gear.weapon());
		assertEquals(Set.of(equipment), gear.equipment());
		assertEquals(Percentile.HALF, gear.noise());
		assertEquals(Optional.of("vocation"), gear.vocation());
		assertNotNull(gear.skills());
		assertEquals(1, gear.skills().stream().count());
		assertEquals(true, gear.skills().contains(skill));
		assertEquals(skill, gear.language());
		assertEquals(Percentile.HALF, gear.tracks());

		// Check behaviour
		final Behaviour behaviour = race.behaviour();
		assertNotNull(behaviour);
		assertEquals(MovementManager.IDLE, behaviour.movement());
		assertEquals(Percentile.HALF, behaviour.aggression());
		assertEquals(Percentile.HALF, behaviour.autoflee());
		assertEquals(true, behaviour.isFlag(Behaviour.Flag.STARTLED));

		// Check kill descriptor
		final Kill kill = race.kill();
		assertNotNull(kill);
		assertNotNull(kill.corpse());
		assertEquals(true, kill.corpse().isPresent());
		assertEquals(Optional.of(butcher), kill.butcher());

		// Check corpse
		final ObjectDescriptor corpse = kill.corpse().get();
		assertEquals("corpse.race", corpse.name());
		assertEquals(1, corpse.properties().weight());
		assertEquals(Size.LARGE, corpse.properties().size());
	}

	@Test
	public void butcherWithoutCorpse() {
		assertThrows(IllegalArgumentException.class, () -> new Race.Kill(null, mock(LootFactory.class)));
	}

	@Test
	public void idleMovementManagerWithPeriod() {
		assertThrows(IllegalArgumentException.class, () -> new Race.Builder("race").movement(MovementManager.IDLE).period(Duration.ofMillis(1)).build());
	}

	@Test
	public void movementManagerZeroPeriod() {
		assertThrows(IllegalArgumentException.class, () -> new Race.Builder("race").movement(mock(MovementManager.class)).period(Duration.ZERO).build());
	}
}
