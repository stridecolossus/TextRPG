package org.sarge.textrpg.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.Hidden;

/**
 * A group is a set of entities such as a pack of dogs or a player party.
 * @author Sarge
 */
public class Group extends AbstractObject {
	/**
	 * Group for an un-grouped entity.
	 */
	public static final Group NONE = new Group() {
		@Override
		public Entity leader() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(Entity entity) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean perceives(Entity actor, Hidden hidden) {
			return false;
		}
	};

	private List<Entity> members = new ArrayList<>();

	/**
	 * Constructor.
	 * @param leader Leader
	 */
	public Group(Entity leader) {
		add(leader);
	}

	/**
	 * Empty group constructor.
	 */
	private Group() {
	}

	/**
	 * @return Members of this group
	 */
	public Stream<Entity> members() {
		return members.stream();
	}

	/**
	 * @return Leader of this group
	 */
	public Entity leader() {
		return members.get(0);
	}

	/**
	 * Sets the leader of the group.
	 * @param leader New leader
	 * @throws IllegalArgumentException if the given entity is already the leader of the group
	 */
	public void leader(Entity leader) {
		if(leader == leader()) throw new IllegalArgumentException("Already leader: " + leader);
		if(!members.contains(leader)) throw new IllegalArgumentException("Not a member: " + leader);
		members.remove(leader);
		members.add(0, leader);
	}

	/**
	 * Adds an entity to this group.
	 * @param entity Entity to add
	 * @throws IllegalArgumentException if the given entity is already grouped
	 */
	public void add(Entity entity) {
		if(entity.model().group() != NONE) throw new IllegalArgumentException("Already grouped: " + entity);
		if(members.contains(entity)) throw new IllegalArgumentException("Already a member: " + entity);
		members.add(entity);
		entity.model().group(this);
	}

	/**
	 * Removes an entity from this group.
	 * @param entity Entity to remove
	 * @throws IllegalArgumentException if the given entity is not a member of this group or is the current leader
	 */
	public void remove(Entity entity) {
		if(!members.contains(entity)) throw new IllegalArgumentException("Not a member: " + entity);
		if(entity == leader()) throw new IllegalArgumentException("Cannot remove leader: " + entity);
		members.remove(entity);
		entity.model().group(NONE);
	}

	/**
	 * Disbands this group.
	 * @throws IllegalArgumentException if this group has already been disbanded
	 */
	public void disband() {
		if(members.isEmpty()) throw new IllegalArgumentException("Already disbanded");
		members.forEach(e -> e.model().group(NONE));
		members.clear();
	}

	/**
	 * Tests whether any member of this group can perceive the given partially hidden object.
	 * @param hidden Hidden object
	 * @return Whether perceived
	 */
	public boolean perceives(Entity actor, Hidden hidden) {
		return members.stream().filter(e -> e != actor).anyMatch(e -> (e == hidden) || e.perceives(hidden));
	}
}
