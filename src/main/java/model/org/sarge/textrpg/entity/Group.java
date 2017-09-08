package org.sarge.textrpg.entity;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;

/**
 * Group or party of entities.
 * @author Sarge
 */
public class Group {
	private final List<Entity> group = new StrictList<>();

	/**
	 * Constructor.
	 * @param leader Group leader
	 * @throws ActionException if the given entity is already in a group
	 */
	public Group(Entity leader) throws ActionException {
		if(leader.group().isPresent()) throw new ActionException("group.cannot.create");
		group.add(leader);
		leader.setGroup(this);
	}
	
	/**
	 * Constructor for a group.
	 * @param group Group members
	 */
	public Group(List<? extends Entity> group) {
		Check.notEmpty(group);
		for(Entity e : group) {
			this.group.add(e);
			e.setGroup(this);
		}
	}
	
	/**
	 * @return Members of this group
	 */
	public Stream<Entity> members() {
		return group.stream();
	}

	/**
	 * @return Leader of this group
	 */
	public Entity leader() {
		return group.get(0);
	}

	/**
	 * Adds an entity to this group
	 * @param e Entity to add
	 * @throws ActionException if the entity is already a member or is in another group
	 */
	protected void add(Entity e) throws ActionException {
		if(group.contains(e)) throw new ActionException("group.add.member");
		if(e.group().isPresent()) throw new ActionException("group.already.grouped");
		group.add(e);
		e.setGroup(this);
	}

	/**
	 * Removes an entity from this group.
	 * @param e Entity to remove
	 * @throws ActionException if the given entity is not a member of this group or is the leader
	 */
	protected void remove(Entity e) throws ActionException {
		if(!group.contains(e)) throw new ActionException("group.not.member");
		if(e == leader()) throw new ActionException("group.remove.leader");
		group.remove(e);
		e.setGroup(null);
	}
	
	/**
	 * Changes the leader of this group.
	 * @param leader New leader
	 * @throws ActionException if the given entity is not a member of this group or is already the leader
	 */
	protected void setLeader(Entity leader) throws ActionException {
		if(!group.contains(leader)) throw new ActionException("group.invalid.leader");
		if(leader == leader()) throw new ActionException("group.already.leader");
		group.remove(leader);
		group.add(0, leader);
	}

	/**
	 * Disbands this group.
	 */
	protected void disband() {
		if(group.isEmpty()) throw new IllegalStateException("Already disbanded");
		group.forEach(e -> e.setGroup(null));
		group.clear();
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
