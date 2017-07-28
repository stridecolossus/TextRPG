package org.sarge.textrpg.entity;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;

public class GroupTest extends ActionTest {
	private Group group;
	private Entity leader;
	private Entity member;
	
	private static Entity createEntity() {
		final Entity e = mock(Entity.class);
		when(e.getGroup()).thenReturn(Optional.empty());
		return e;
	}
	
	@Before
	public void before() throws ActionException {
		leader = createEntity();
		member = createEntity();
		group = new Group(leader);
	}
	
	@Test
	public void constructor() {
		assertEquals(leader, group.getLeader());
		assertArrayEquals(new Entity[]{leader}, group.getMembers().toArray());
		verify(leader).setGroup(group);
	}
	
	@SuppressWarnings("unused")
	@Test
	public void constructorAlreadyGrouped() throws ActionException {
		when(leader.getGroup()).thenReturn(Optional.of(group));
		expect("group.cannot.create");
		new Group(leader);
	}
	
	@Test
	public void add() throws ActionException {
		group.add(member);
		assertArrayEquals(new Entity[]{leader, member}, group.getMembers().toArray());
		verify(member).setGroup(group);
	}
	
	@Test
	public void addDuplicate() throws ActionException {
		group.add(member);
		expect("group.add.member");
		group.add(member);
	}
	
	@Test
	public void addAlreadyGrouped() throws ActionException {
		when(member.getGroup()).thenReturn(Optional.of(mock(Group.class)));
		expect("group.already.grouped");
		group.add(member);
	}
	
	@Test
	public void remove() throws ActionException {
		group.add(member);
		group.remove(member);
		verify(member).setGroup(group);
		verify(member).setGroup(null);
		assertArrayEquals(new Entity[]{leader}, group.getMembers().toArray());
	}
	
	@Test
	public void removeNotMember() throws ActionException {
		expect("group.not.member");
		group.remove(member);
	}
	
	@Test
	public void removeLeader() throws ActionException {
		expect("group.remove.leader");
		group.remove(leader);
	}
	
	@Test
	public void setLeader() throws ActionException {
		group.add(member);
		group.setLeader(member);
		assertEquals(member, group.getLeader());
	}
	
	@Test
	public void setLeaderDuplicate() throws ActionException {
		expect("group.already.leader");
		group.setLeader(leader);
	}
	
	@Test
	public void setLeaderNotMember() throws ActionException {
		expect("group.invalid.leader");
		group.setLeader(member);
	}
	
	@Test
	public void disband() throws ActionException {
		group.add(member);
		group.disband();
		verify(leader).setGroup(null);
		verify(member).setGroup(null);
		assertEquals(0, group.getMembers().count());
	}
	
	@Test(expected = IllegalStateException.class)
	public void disbandAlreadyDisbanded() {
		group.disband();
		group.disband();
	}
}
