package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Hidden;

public class GroupTest extends ActionTestBase {
	private Entity entity;
	private Group group;

	@BeforeEach
	public void before() {
		final EntityModel model = mock(EntityModel.class);
		entity = mock(Entity.class);
		when(model.group()).thenReturn(Group.NONE);
		when(entity.model()).thenReturn(model);

		when(actor.model().group()).thenReturn(Group.NONE);
		group = new Group(actor);
	}

	@Test
	public void constructor() {
		assertNotNull(group.members());
		assertEquals(1, group.members().count());
		assertEquals(actor, group.members().iterator().next());
		assertEquals(actor, group.leader());
		verify(actor.model()).group(group);
	}

	@SuppressWarnings("unused")
	@Test
	public void groupAlreadyGrouped() {
		new Group(actor);
	}

	@Test
	public void add() {
		group.add(entity);
		verify(entity.model()).group(group);
		assertArrayEquals(new Entity[]{actor, entity}, group.members().toArray());
		assertEquals(actor, group.leader());
	}

	@Test
	public void addAlreadyMember() {
		assertThrows(IllegalArgumentException.class, () -> group.add(actor));
	}

	@Test
	public void addAlreadyGrouped() {
		when(entity.model().group()).thenReturn(group);
		assertThrows(IllegalArgumentException.class, () -> group.add(entity));
	}

	@Test
	public void addInvalidEntity() {
		// TODO
	}

	@Test
	public void remove() {
		group.add(entity);
		group.remove(entity);
		assertEquals(1, group.members().count());
		verify(entity.model()).group(Group.NONE);
	}

	@Test
	public void removeNotMember() {
		assertThrows(IllegalArgumentException.class, () -> group.remove(entity));
	}

	@Test
	public void removeLeader() {
		assertThrows(IllegalArgumentException.class, () -> group.remove(actor));
	}

	@Test
	public void setLeader() {
		group.add(entity);
		group.leader(entity);
		assertEquals(entity, group.leader());
	}

	@Test
	public void setLeaderAlreadyLeader() {
		assertThrows(IllegalArgumentException.class, () -> group.leader(actor));
	}

	@Test
	public void setLeaderNotMember() {
		assertThrows(IllegalArgumentException.class, () -> group.leader(entity));
	}

	@Test
	public void disband() {
		group.add(entity);
		group.disband();
		assertEquals(0, group.members().count());
		verify(actor.model()).group(Group.NONE);
		verify(entity.model()).group(Group.NONE);
	}

	@Test
	public void disbandAlreadyDisbanded() {
		group.disband();
		assertThrows(IllegalArgumentException.class, () -> group.disband());
	}

	@Test
	public void perceives() {
		// Check ignores actor
		assertEquals(false, group.perceives(actor, actor));

		// Check perceives group members
		group.add(entity);
		assertEquals(true, group.perceives(actor, entity));
		assertEquals(true, group.perceives(entity, actor));

		// Check group does not perceive hidden objects
		final var hidden = mock(Hidden.class);
		assertEquals(false, group.perceives(actor, hidden));

		// Check actor perceives objects known to other members
		when(entity.perceives(hidden)).thenReturn(true);
		assertEquals(true, group.perceives(actor, hidden));
	}
}
