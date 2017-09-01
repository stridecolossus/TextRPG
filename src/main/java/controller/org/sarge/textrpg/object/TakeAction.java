package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.Entity;

/**
 * Action to pick up an object.
 * @author Sarge
 */
public class TakeAction extends AbstractAction {
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Helper - Move object to inventory.
	 * @param actor		Actor
	 * @param obj		Object to take
	 * @throws ActionException if the object cannot be carried or is too heavy
	 */
	private static void takeObject(Entity actor, WorldObject obj) throws ActionException {
		// Check can be carried
		obj.take(actor);

		// Check not too heavy
		final int weight = obj.weight();
		if(weight > actor.getAttributes().get(Attribute.STRENGTH)) {		// TODO - modifier?
			throw new ActionException("take.too.heavy");
		}

		// Add to inventory
		obj.setParent(actor);
	}

	/**
	 * Take an object.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @throws ActionException
	 */
	public ActionResponse take(Entity actor, WorldObject obj) throws ActionException {
		takeObject(actor, obj);
		return response(obj);
	}

	/**
	 * Take an object from a container.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @param container
	 * @throws ActionException
	 */
	public ActionResponse take(Entity actor, WorldObject obj, Container container) throws ActionException {
		if(!container.getOpenableModel().map(Openable::isOpen).orElse(true)) throw new ActionException("take.container.closed", container);
		takeObject(actor, obj);
		return response(obj);
	}

	/**
	 * Take objects(s) matching the given filter.
	 * @param ctx			Context
	 * @param actor			Actor
	 * @param filter		Filter
	 * @return Response
	 * @throws ActionException
	 */
	public ActionResponse take(Entity actor, ObjectFilter filter) throws ActionException {
		// Enumerate results
		final List<WorldObject> results = ContentsHelper.select(actor.getLocation().getContents().stream(), WorldObject.class)
			.filter(obj -> filter.test(obj.getDescriptor()))
			.collect(toList());

		// Check for no results
		if(results.isEmpty()) throw new ActionException("take.no.results");

		// Take results
		final List<Description> responses = new ArrayList<>();
		for(final WorldObject obj : results) {
			try {
				takeObject(actor, obj);
				responses.add(new Description("take.response", "name", obj));
			}
			catch(final ActionException e) {
				responses.add(new Description(e.getMessage()));
			}
		}
		return new ActionResponse(responses);
	}
}
