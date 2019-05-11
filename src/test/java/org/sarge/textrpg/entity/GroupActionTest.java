package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class GroupActionTest extends ActionTestBase {
	private GroupAction action;
	private Group group;
	private Entity entity;

	@BeforeEach
	public void before() {
		// Create group for actor
		group = mock(Group.class);
		when(actor.model().group()).thenReturn(group);
		when(group.leader()).thenReturn(actor);

		// Create entity/member
		final EntityModel model = mock(EntityModel.class);
		entity = mock(Entity.class);
		when(entity.model()).thenReturn(model);
		when(model.group()).thenReturn(Group.NONE);
		when(entity.name()).thenReturn("entity");

		// Create action
		action = new GroupAction();
	}

	@Test
	public void add() throws ActionException {
		assertEquals(Response.OK, action.add(actor, entity));
		verify(group).add(entity);
	}

	@Disabled("check gets over-ridden by test for whether a member") // TODO
	@Test
	public void addAlreadyMember() throws ActionException {
		when(entity.model().group()).thenReturn(group);
		TestHelper.expect("add.already.member", () -> action.add(actor, entity));
	}

	@Test
	public void addAlreadyGrouped() throws ActionException {
		when(entity.model().group()).thenReturn(mock(Group.class));
		TestHelper.expect("add.already.grouped", () -> action.add(actor, entity));
	}

	@Test
	public void addInvalidEntity() throws ActionException {
		when(actor.isValidTarget(entity)).thenReturn(true);
		TestHelper.expect("add.invalid.entity", () -> action.add(actor, entity));
	}

	@Test
	public void addNotLeader() throws ActionException {
		final Group other = new Group(entity);
		when(actor.model().group()).thenReturn(other);
		TestHelper.expect("add.not.leader", () -> action.add(actor, entity));
	}

	@Test
	public void create() throws ActionException {
		// Create group
		when(actor.model().group()).thenReturn(Group.NONE);
		assertEquals(Response.OK, action.add(actor, entity));

		// Check new group created
		final ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
		verify(actor.model()).group(captor.capture());

		// Check group
		final Group group = captor.getValue();
		assertNotNull(group);
		assertEquals(actor, group.leader());
		assertArrayEquals(new Entity[]{actor, entity}, group.members().toArray());
	}

	@Test
	public void remove() throws ActionException {
		when(entity.model().group()).thenReturn(group);
		assertEquals(Response.EMPTY, action.remove(actor, entity));
		verify(group).remove(entity);
	}

	@Test
	public void removeNotMember() throws ActionException {
		TestHelper.expect("remove.not.member", () -> action.remove(actor, entity));
	}

	@Test
	public void leave() throws ActionException {
		when(group.leader()).thenReturn(entity);
		assertEquals(Response.OK, action.leave(actor));
		verify(group).remove(actor);
	}

	@Test
	public void leaveNotGrouped() throws ActionException {
		when(actor.model().group()).thenReturn(Group.NONE);
		TestHelper.expect("leave.not.grouped", () -> action.leave(actor));
	}

	@Test
	public void leaveGroupLeader() throws ActionException {
		TestHelper.expect("leave.cannot.leader", () -> action.leave(actor));
	}

	@Test
	public void leader() throws ActionException {
		when(entity.model().group()).thenReturn(group);
		assertEquals(Response.EMPTY, action.leader(actor, entity));
		verify(group).leader(entity);
	}

	@Test
	public void leaderNotMember() throws ActionException {
		TestHelper.expect("leader.not.member", () -> action.leader(actor, entity));
	}

	@Test
	public void leaderAlreadyLeader() throws ActionException {
		TestHelper.expect("leader.already.leader", () -> action.leader(actor, actor));
	}

	@Test
	public void disband() throws ActionException {
		assertEquals(Response.OK, action.disband(actor));
		verify(group).disband();
	}

	@Test
	public void list() throws ActionException {
		when(group.members()).thenReturn(Stream.of(actor, entity));
		final Response response = action.list(actor);
		final Response expected = new Response.Builder()
			.add("group.list.header")
			.add(new Description.Builder("group.member").add("name", "actor", ArgumentFormatter.PLAIN).build())
			.add(new Description.Builder("group.member").add("name", "entity", ArgumentFormatter.PLAIN).build())
			.build();
		assertEquals(expected, response);
	}

	@Test
	public void listNotGrouped() throws ActionException {
		when(actor.model().group()).thenReturn(Group.NONE);
		TestHelper.expect("list.not.grouped", () -> action.list(actor));
	}

	@Test
	public void broadcast() {
		final Description expected = new Description.Builder("message").add("name", "entity", ArgumentFormatter.PLAIN).build();
		when(group.members()).thenReturn(Stream.of(actor, entity));
		GroupAction.broadcast("message", group, entity);
		verify(actor).alert(expected);
	}
}
