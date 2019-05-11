package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Equipment;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.springframework.stereotype.Component;

/**
 * Action to put objects into a container.
 * @author Sarge
 */
@Component
public class PutAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public PutAction() {
		super(Flag.BROADCAST);
	}

	/**
	 * Puts an object into the given container.
	 * @param obj				Object to move
	 * @param container			Container
	 * @return Response
	 */
	public Response put(@Carried WorldObject obj, Parent parent) {
		return Response.of(move(obj, parent));
	}

	/**
	 * Puts matching objects into the given container.
	 * @param filter		Filter
	 * @param container		Container
	 * @return Response
	 * @throws ActionException if there are no matching objects
	 */
	@RequiresActor
	public Response put(Entity actor, ObjectDescriptor.Filter filter, Parent parent) throws ActionException {
		// Enumerate matching objects
		// TODO - do we need to check for perceived objects?
		final Equipment equipment = actor.contents().equipment();
		final var results = actor.contents().select(WorldObject.class)
			.filter(WorldObject.Filter.of(filter))
			.filter(StreamUtil.not(equipment::contains))
			.map(obj -> move(obj, parent))
			.collect(toList());

		// Build response
		if(results.isEmpty()) {
			throw ActionException.of("put.empty.filter");
		}
		else {
			return Response.of(results);
		}
	}

	/**
	 * Helper - Moves an object into the given parent.
	 */
	private static Description move(WorldObject obj, Parent parent) {
		final var reason = parent.contents().reason(obj);
		if(reason.isPresent()) {
			return new Description(reason.get(), obj.name());
		}
		else {
			obj.parent(parent);
			return new Description.Builder("put.object.response").name(obj.name()).add("parent", parent.name()).build();
		}
	}
}
