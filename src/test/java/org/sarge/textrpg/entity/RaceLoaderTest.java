package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.entity.Race.Behaviour;
import org.sarge.textrpg.object.DefaultObjectDescriptorLoader;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.LootFactoryLoader;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptorLoader;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Registry;

public class RaceLoaderTest {
	@SuppressWarnings("unchecked")
	@Test
	public void load() {
		// Create butcher loot-factory loader
		final var loot = mock(LootFactoryLoader.class);
		when(loot.load(any())).thenReturn(LootFactory.EMPTY);

		// Create skills
		final Registry<Skill> skills = mock(Registry.class);
		when(skills.get("skill")).thenReturn(Skill.NONE);

		// Create loader
		final DefaultObjectDescriptorLoader descriptorLoader = mock(DefaultObjectDescriptorLoader.class);
		final RaceLoader loader = new RaceLoader(descriptorLoader, loot, skills);

		// Create XML
		final Element xml = new Element.Builder("race")
			// Characteristics
			.attribute("name", "name")
			.attribute("faction", "faction")
			.attribute("gender", Gender.FEMALE)
			.attribute("alignment", Alignment.EVIL)
			.attribute("size", Size.MEDIUM)
			.attribute("weight", 1)
			// Attributes
			.child("attribute")
				.attribute("attr", Attribute.AGILITY)
				.attribute("value", 2)
			.end()
			// Body parts
			.child("body").text("part").end()
			// Categories
			.child("category")
				.text("cat")
				.end()
			// Gear
			.add("weapon")
			.add("equipment")
			.attribute("noise", 50)
			.attribute("tracks", 50)
			// Skills
			.child("skill").text("skill").end()
			.attribute("vocation", "vocation")
			.attribute("language", "lang")
			// Behaviour
			.attribute("aggression", 50)
			.attribute("autoflee", 50)
			.attribute("period", "2s")
			.child("behaviour")
				.add("startled")
			.end()
			.child("movement")
				.attribute("type", "terrain")
				.add("grassland")
				.end()
			// Kill descriptor
			.add("butcher")
			.build();

		// Init descriptor loader
		final Weapon.Descriptor descriptor = mock(Weapon.Descriptor.class);
		when(descriptorLoader.load(xml.child("weapon"), ObjectDescriptorLoader.Policy.OBJECT)).thenReturn(descriptor);
		when(descriptorLoader.load(xml.child("equipment"), ObjectDescriptorLoader.Policy.OBJECT)).thenReturn(ObjectDescriptor.of("equipment"));
		when(descriptor.create()).thenReturn(mock(Weapon.class));

		// Load race
		final Race race = loader.load(xml);
		assertNotNull(race);
		assertEquals("name", race.name());

		// Check characteristics
		final Race.Characteristics chars = race.characteristics();
		assertNotNull(chars);
		assertEquals(Gender.FEMALE, chars.gender());
		assertEquals(Alignment.EVIL, chars.alignment());
		assertEquals(Size.MEDIUM, chars.size());
		assertEquals(1, chars.weight());
		assertEquals(2, chars.attributes().get(Attribute.AGILITY).get());
		assertArrayEquals(new String[]{"part"}, chars.body().toArray());
		assertEquals(true, chars.isCategory("cat"));

		// Check equipment
		final Race.Gear gear = race.gear();
		assertNotNull(gear);
		assertEquals(Set.of(ObjectDescriptor.of("equipment")), gear.equipment());
		assertNotNull(gear.weapon());
		assertEquals(Percentile.HALF, gear.noise());
		assertEquals(Percentile.HALF, gear.tracks());

		// Check skills
		assertEquals(Optional.of("vocation"), gear.vocation());
		assertEquals(Skill.NONE, gear.language());
		assertEquals(1, gear.skills().stream().count());

		// Check behaviour
		final Race.Behaviour behaviour = race.behaviour();
		assertEquals(Percentile.HALF, behaviour.aggression());
		assertEquals(Percentile.HALF, behaviour.autoflee());
		assertEquals(true, behaviour.isFlag(Behaviour.Flag.STARTLED));
		assertNotNull(behaviour.movement());

		// Check corpse
		final Race.Kill kill = race.kill();
		assertNotNull(kill);
		assertNotNull(kill.corpse());
		assertEquals(true, kill.corpse().isPresent());
		assertEquals(Optional.of(LootFactory.EMPTY), kill.butcher());
	}
}
