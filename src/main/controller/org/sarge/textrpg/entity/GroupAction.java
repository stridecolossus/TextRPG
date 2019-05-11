package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.function.Function;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.Terrain;
import org.springframework.stereotype.Component;

/**
 * Action to manipulate an entities group.
 * @author Sarge
 * @see Entity#group(Group)
 */
@Component
@RequiresActor
public class GroupAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public GroupAction() {
		super(Flag.OUTSIDE);
	}

	@Override
	public String prefix() {
		return "group";
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	protected boolean isValid(Stance stance) {
		return true;
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Finds and verifies the group for the given actor.
	 * @param actor Actor
	 * @return Group
	 * @throws ActionException if the actor is not grouped or is not the leader
	 */
	private static Group group(Entity actor) throws ActionException {
		final Group group = actor.model().group();
		if(group == Group.NONE) throw ActionException.of("group.not.grouped");
		if(group.leader() != actor) throw ActionException.of("group.not.leader");
		return group;
	}

	/**
	 * Broadcasts a message to all members of the given group except the specified entity.
	 * @param message		Message
	 * @param group			Group
	 * @param actor			Entity to ignore
	 */
	static void broadcast(String message, Group group, Entity actor) {
		final Description alert = new Description.Builder(message).add("name", actor.name(), ArgumentFormatter.PLAIN).build();
		Actor.broadcast(actor, alert, group.members());
	}

	/**
	 * Adds the given entity to the group.
	 * @param actor			Actor
	 * @param entity		Entity to add
	 * @return Response
	 * @throws ActionException if the actor is not the leader, the entity is already grouped, or the entity cannot be added
	 */
	public Response add(Entity actor, Entity entity) throws ActionException {
		// Check entity can be added
		final Group group = actor.model().group();
		if(entity.model().group() != Group.NONE) throw ActionException.of("add.already.grouped");
		if(actor.isValidTarget(entity)) throw ActionException.of("add.invalid.entity");

		// Add to group
		if(group == Group.NONE) {
			final Group newGroup = new Group(actor);
			newGroup.add(entity);
		}
		else {
			if(group.leader() != actor) throw ActionException.of("add.not.leader");
			// TODO - if(entity.group() == group) throw ActionException.of("add.already.member");
			group.add(entity);
		}

		// Notify group
		broadcast("group.member.added", group, entity);
		entity.alert(new Description("group.added", actor.name()));
		return Response.OK;
	}

	/**
	 * Removes the given entity from the group.
	 * @param actor			Actor
	 * @param entity		Entity to remove
	 * @return Response
	 * @throws ActionException if the entity is not a member of the group
	 */
	public Response remove(Entity actor, Entity entity) throws ActionException {
		final Group group = group(actor);
		if(entity.model().group() != group) throw ActionException.of("remove.not.member");
		group.remove(entity);

		// Notify group
		broadcast("group.member.removed", group, entity);
		entity.alert(new Description("group.removed", actor.name()));
		return Response.EMPTY;
	}

	/**
	 * Leaves the group.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor is not grouped or is the group leader
	 */
	public Response leave(Entity actor) throws ActionException {
		// Leave current group
		final Group group = actor.model().group();
		if(group == Group.NONE) throw ActionException.of("leave.not.grouped");
		if(group.leader() == actor) throw ActionException.of("leave.cannot.leader");
		group.remove(actor);

		// Notify group
		broadcast("group.member.leave", group, actor);
		return Response.OK;
	}

	/**
	 * Sets the leader of the group.
	 * @param actor			Actor
	 * @param entity		New leader
	 * @return Response
	 * @throws ActionException if the given entity is not a member of the group or is already the leader
	 */
	public Response leader(Entity actor, Entity entity) throws ActionException {
		// Set new group leader
		final Group group = group(actor);
		if(entity.model().group() != group) throw ActionException.of("leader.not.member");
		if(group.leader() == entity) throw ActionException.of("leader.already.leader");
		group.leader(entity);

		// Notify group
		broadcast("group.leader.changed", group, entity);
		entity.alert(Description.of("group.leader"));
		return Response.EMPTY;
	}

	/**
	 * Disbands the group.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor is not the leader of the group
	 */
	public Response disband(Entity actor) throws ActionException {
		final Group group = group(actor);
		broadcast("group.disbanded", group, actor);
		group.disband();
		return Response.OK;
	}

	/**
	 * Lists the members of the group.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor is not grouped
	 */
	public Response list(Entity actor) throws ActionException {
		if(actor.model().group() == Group.NONE) throw ActionException.of("list.not.grouped");
		final Function<String, Description> mapper = name -> new Description.Builder("group.member").add("name", name, ArgumentFormatter.PLAIN).build();
		final var members = actor.model().group().members().map(Entity::name).map(mapper).collect(toList());
		return new Response.Builder().add("group.list.header").add(members).build();
	}
}
