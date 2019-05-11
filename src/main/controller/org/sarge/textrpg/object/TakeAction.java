package org.sarge.textrpg.object;

import java.util.stream.Stream;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.entity.CarryController.Result;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Event;
import org.springframework.stereotype.Component;

/**
 * Action to carry an object.
 * @author Sarge
 * TODO - take n from container => either n objects or stack(n)
 */
@Component
@RequiresActor
public class TakeAction extends AbstractAction {
	/**
	 * Inventory controller which ignores failed carry results and handles dispensers.
	 */
	private final InventoryController inv = new InventoryController("take") {
		@Override
		protected void take(Entity actor, WorldObject obj, Result result) {
			if(result.isCarried()) {
				// Delegate
				super.take(actor, obj, result);

				// Refresh dispensers
				final ObjectDescriptor descriptor = obj.descriptor();
				if(descriptor instanceof Dispenser) {
					refresh((Dispenser) descriptor, actor.location());
				}
			}
		}
	};

	private final Event.Queue queue;

	/**
	 * Constructor.
	 * @param manager Queue manager
	 */
	public TakeAction(Event.Queue.Manager manager) {
		super(Flag.OUTSIDE, Flag.BROADCAST);
		this.queue = manager.queue("refresh.dispenser");
	}

	/**
	 * Carries an object.
	 * @param actor			Actor
	 * @param obj			Object to carry
	 * @return Response
	 */
	public Response take(Entity actor, WorldObject obj) {
		return Response.of(inv.take(actor, obj));
	}

	/**
	 * Takes an object from the given container.
	 * @param actor			Actor
	 * @param obj			Object to take
	 * @param parent		Parent
	 * @return Response
	 * @throws ActionException if the object is not in the given container
	 */
	public Response take(Entity actor, WorldObject obj, Parent parent) throws ActionException {
		if(obj.parent() != parent.contents()) throw ActionException.of("take.container.invalid");
		return take(actor, obj);
	}

	/**
	 * Takes objects matching the given filter.
	 * @param actor			Actor
	 * @param filter		Filter
	 * @return Response
	 */
	@ActionOrder(2)
	public Response take(Entity actor, ObjectDescriptor.Filter filter) {
		final var objects = find(actor.contents(), filter);
		return Response.of(inv.take(actor, objects));
	}

	/**
	 * Takes objects matching the given filter from a container.
	 * @param actor			Actor
	 * @param filter		Filter
	 * @param container		Container
	 * @return Response
	 */
	@ActionOrder(2)
	public Response take(Entity actor, ObjectDescriptor.Filter filter, Container container) {
		final var objects = find(container.contents(), filter);
		return Response.of(inv.take(actor, objects));
	}

	/**
	 * Helper - Finds all object in the given contents matching the specified filter.
	 */
	private static Stream<WorldObject> find(Contents contents, ObjectDescriptor.Filter filter) {
		return contents.select(WorldObject.class).filter(WorldObject.Filter.of(filter));
	}

	/**
	 * Refreshes a dispenser.
	 * @param dispenser		Dispenser
	 * @param parent		Parent location
	 */
	private void refresh(Dispenser dispenser, Parent parent) {
		// Record dispensed object
		dispenser.dispense();

		// Register refresh event
		final Event event = () -> {
			// Generate new object
			final WorldObject obj = dispenser.create();
			obj.parent(parent);

			// Record dispensed object
			dispenser.restore();
			return false;
		};
		queue.add(event, dispenser.refresh());
	}
}
