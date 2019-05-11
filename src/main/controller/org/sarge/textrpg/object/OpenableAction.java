package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.EnumAction;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Openable.Operation;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Location;

/**
 * Action to manipulate an {@link Openable}.
 * @author Sarge
 */
@EnumAction(Openable.Operation.class)
public class OpenableAction extends AbstractAction {
	private final ObjectController controller;

	/**
	 * Constructor.
	 * @param controller Controller for reset events
	 */
	public OpenableAction(ObjectController controller) {
		super(Flag.BROADCAST);
		this.controller = notNull(controller);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	/**
	 * Applies an openable operation to the given container.
	 * @param actor			Actor
	 * @param op			Openable operation
	 * @param container		Container
	 * @return Response
	 * @throws ActionException if the operation cannot be applied
	 */
	@RequiresActor
	public Response execute(Entity actor, Operation op, OpenableContainer container) throws ActionException {
		// Delegate
		executeLocal(actor, op, container, container.name());

		// Register reset event
		final Event reset = () -> {
			container.model().reset();
			broadcast("container.auto.reset", container, actor.location());
			return false;
		};
		controller.reset(container, reset);

		return Response.OK;
	}

	/**
	 * Applies an openable operation to the given portal.
	 * @param actor			Actor
	 * @param op			Openable operation
	 * @param portal		Portal
	 * @return Response
	 * @throws ActionException if the operation cannot be applied
	 */
	@RequiresActor
	public Response execute(Entity actor, Operation op, Portal portal) throws ActionException {
		// Delegate
		executeLocal(actor, op, portal, portal.name());

		// Notify other side of portal
		final Location loc = actor.location();
		final Location other = controller.other(loc, portal);
		final String key = TextHelper.join("portal.notification", op.name());
		other.broadcast(null, new Description(key, portal.name()));

		// Broadcast reset notification to both sides of the gate
		final Event reset = () -> {
			final Description alert = new Description("portal.auto.reset", portal.name());
			loc.broadcast(null, alert);
			other.broadcast(null, alert);
			portal.model().reset();
			return false;
		};
		controller.reset(portal, reset);

		return Response.OK;
	}

	/**
	 * Opens or closes a window.
	 * @param actor			Actor
	 * @param op			Operation
	 * @param window		Window
	 * @return Response
	 * @throws ActionException if the operation cannot be applied to the window
	 */
	@RequiresActor
	@ActionOrder(2)
	public Response execute(Entity actor, Operation op, Window window) throws ActionException {
		// Delegate
		executeLocal(actor, op, window, window.name());

		// Register reset event
		final Event reset = () -> {
			window.model().reset();
			broadcast("window.auto.reset", window, actor.location());
			return false;
		};
		controller.reset(window, reset);

		return Response.OK;
	}

	/**
	 * Applies this action to the given openable.
	 * @param actor			Actor
	 * @param op			Openable operation
	 * @param openable		Openable object
	 * @param name			Name of the owner of the openable
	 * @throws ActionException if the action fails
	 */
	private static void executeLocal(Entity actor, Operation op, Openable openable, String name) throws ActionException {
		// Check for required key
		final Openable.Model model = openable.model();
		if(op.isLocking() && model.isLockable() && (model.lock() != Openable.Lock.LATCH)) {
			final ObjectDescriptor key = model.lock().key();
			final WorldObject.Filter filter = WorldObject.Filter.of(ObjectDescriptor.Filter.of(key));
			if(!actor.contents().find(filter).isPresent()) throw ActionException.of("openable.requires.key");
		}

		// Apply operation
		try {
			model.apply(op);
		}
		catch(Openable.OpenableException e) {
			throw new ActionException(new Description(e.getMessage(), name));
		}
	}

	/**
	 * Broadcasts a reset event.
	 * @param key Notification key
	 * @param obj Object
	 * @param loc Location
	 */
	private static void broadcast(String key, WorldObject obj, Location loc) {
		final Description alert = new Description(key, obj.name());
		loc.broadcast(null, alert);
	}
}
