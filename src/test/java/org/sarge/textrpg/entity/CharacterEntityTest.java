package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.entity.Race.Builder;
import org.sarge.textrpg.util.MutableIntegerMap;

public class CharacterEntityTest extends ActionTest {
	private CharacterEntity character, other;
	private Race race;
	private Topic topic;
	
	@Before
	public void before() {
		race = new Builder("race").build();
		topic = new Topic("topic", Script.NONE);
		character = new CharacterEntity("name", race, new MutableIntegerMap<>(Attribute.class), EntityManager.IDLE, Gender.FEMALE, Alignment.EVIL, Collections.singletonList(topic));
		other = new CharacterEntity("other", race, new MutableIntegerMap<>(Attribute.class), EntityManager.IDLE, Gender.FEMALE, Alignment.EVIL, Collections.emptyList());
	}
	
	@Test
	public void constructor() {
		assertEquals("name", character.getName());
		assertEquals(Gender.FEMALE, character.getGender());
		assertEquals(Alignment.EVIL, character.getAlignment());
		assertEquals(Contents.EMPTY, character.getContents());
		assertNotNull(character.getTopics());
		assertEquals(1, character.getTopics().count());
		assertEquals(topic, character.getTopics().iterator().next());
		assertEquals(Optional.empty(), character.getMount());
		assertNotNull(character.getFollowers());
	}
	
	@Test
	public void follow() throws ActionException {
		character.follow(other);
		assertEquals(true, character.isFollowing(other));
		assertEquals(1, other.getFollowers().count());
		assertEquals(character, other.getFollowers().iterator().next());
	}

	@Test
	public void followStopFollowing() throws ActionException {
		character.follow(other);
		character.follow(null);
		assertEquals(false, character.isFollowing(other));
		assertEquals(0, other.getFollowers().count());
	}
	
	@Test
	public void followSwitchFollowing() throws ActionException {
		character.follow(other);
		character.follow(other);
		assertEquals(true, character.isFollowing(other));
		assertEquals(1, other.getFollowers().count());
		assertEquals(character, other.getFollowers().iterator().next());
	}

	@Test
	public void followNotFollowing() throws ActionException {
		expect("follow.not.following");
		character.follow(null);
	}

	@Test
	public void followInvalid() throws ActionException {
		final CharacterEntity good = new CharacterEntity("name", race, new MutableIntegerMap<>(Attribute.class), EntityManager.IDLE, Gender.FEMALE, Alignment.GOOD, Collections.emptyList());
		expect("follow.entity.invalid");
		character.follow(good);
	}

	@Test
	public void setMount() throws ActionException {
		final Entity mount = mock(Entity.class);
		when(mount.getRace()).thenReturn(new Builder("mount").mount().build());
		when(mount.isFollowing(character)).thenReturn(true);
		character.setMount(mount);
		assertEquals(Optional.of(mount), character.getMount());
		assertEquals(Stance.MOUNTED, character.getStance());
	}

	@Test
	public void setMountDismount() throws ActionException {
		final Entity mount = mock(Entity.class);
		when(mount.getRace()).thenReturn(new Builder("mount").mount().build());
		when(mount.isFollowing(character)).thenReturn(true);
		character.setMount(mount);
		character.setMount(null);
		assertEquals(Optional.empty(), character.getMount());
		assertEquals(Stance.DEFAULT, character.getStance());
	}

	@Test
	public void setMountNotFollowing() throws ActionException {
		final Entity mount = mock(Entity.class);
		when(mount.getRace()).thenReturn(new Builder("mount").mount().build());
		expect("mount.not.leading");
		character.setMount(mount);
	}

	@Test
	public void setMountInvalidMount() throws ActionException {
		final Entity mount = mock(Entity.class);
		when(mount.getRace()).thenReturn(race);
		when(mount.isFollowing(character)).thenReturn(true);
		expect("mount.invalid.mount");
		character.setMount(mount);
	}
}
