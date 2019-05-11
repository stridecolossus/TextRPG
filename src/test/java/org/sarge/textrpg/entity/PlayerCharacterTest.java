package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.PlayerCharacter.PlayerEntityDescriptor;
import org.sarge.textrpg.entity.PlayerCharacter.PlayerModel;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.world.Faction;

public class PlayerCharacterTest {
	private PlayerCharacter player;
	private Consumer<Response> listener;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		final Race race = new Race.Builder("race").build();
		final Faction.Association association = new Faction.Association(mock(Faction.class), Relationship.FRIENDLY);
		final PlayerEntityDescriptor descriptor = new PlayerEntityDescriptor.Builder().name("name").race(race).gender(Gender.FEMALE).alignment(Alignment.EVIL).faction(association.faction()).build();
		final PlayerModel model = new PlayerModel(association);
		listener = mock(Consumer.class);
		player = new PlayerCharacter(descriptor, mock(Event.Queue.class), listener, model);
	}

	@Test
	public void constructor() {
		assertEquals("name", player.name());
		assertEquals(Gender.FEMALE, player.descriptor().gender());
		assertEquals(Alignment.EVIL, player.descriptor().alignment());
		assertNotNull(player.skills());
		assertNotNull(player.descriptor().topics());
		assertEquals(0, player.descriptor().topics().count());
		assertNotNull(player.contents());
		assertEquals(true, player.isPlayer());
		assertNotNull(player.settings());
		assertNotNull(player.hidden());
		assertEquals(null, player.previous());
		assertNotNull(player.player().associations());
		assertEquals(1, player.player().associations().count());
		assertNotNull(player.player().trophy());
	}

	@Test
	public void perceives() {
		final Hidden hidden = mock(Hidden.class);
		player.hidden().add(hidden, Duration.ofMinutes(1));
		assertEquals(true, player.perceives(hidden));
	}

	@Test
	public void previous() {
		final Thing prev = mock(Thing.class);
		player.setPrevious(prev);
		assertEquals(prev, player.previous());
		player.setPrevious(null);
		assertEquals(null, player.previous());
	}

	@Test
	public void addSkill() {
		player.player().skills().add(Skill.NONE);
		assertEquals(true, player.skills().contains(Skill.NONE));
	}

	@Test
	public void isAssociated() {
		final Faction faction = mock(Faction.class);
		final Faction.Association association = new Faction.Association(faction, Relationship.FRIENDLY);
		when(faction.relationship(Alignment.EVIL)).thenReturn(Relationship.NEUTRAL);
		assertEquals(false, player.isAssociated(association));
	}

	@Test
	public void association() {
		final Faction faction = mock(Faction.class);
		final Faction.Association association = new Faction.Association(faction, Relationship.FRIENDLY);
		player.player().add(association);
		assertEquals(true, player.isAssociated(association));
		assertEquals(true, player.isAssociated(new Faction.Association(faction, Relationship.NEUTRAL)));
		assertEquals(false, player.isAssociated(new Faction.Association(faction, Relationship.ALLIED)));
	}

	@Test
	public void stateChangeNotificationLight() {
		assertEquals(false, player.notify(ContentStateChange.LIGHT_MODIFIED));
	}

	@Test
	public void stateChangeNotificationOther() {
		final ContentStateChange notification = ContentStateChange.of(ContentStateChange.Type.OTHER, new Description("key"));
		assertEquals(false, player.notify(notification));
	}
}
