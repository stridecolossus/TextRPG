package org.sarge.textrpg.runner;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sarge.lib.collection.StrictMap;
import org.sarge.textrpg.common.AbstractAction.Flag;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.entity.Transaction;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.parser.Command;
import org.sarge.textrpg.runner.ActionDescriptor.RequiredDescriptor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.LightLevelProvider;
import org.springframework.stereotype.Component;

/**
 * The <i>command executor</i> invokes a {@link Command}.
 * <p>
 * Commands are executed as follows:
 * <ol>
 * <li>Check whether the actor possesses the required skill (if any)</li>
 * <li>Check that the actor has sufficient power (if the action requires a skill)</li>
 * <li>Build the command arguments</li>
 * <li>Invoke the action method</li>
 * <li>Consume power</li>
 * <li>Apply wear to tools</li>
 * <li>Optionally reveal the actor</li>
 * </ol>
 * <p>
 * @author Sarge
 */
@Component
public class CommandExecutor {
	private static final ActionException REQUIRES_LIGHT = ActionException.of("action.invalid.light");

	/**
	 * Executes the given command.
	 * @return Response
	 * @throws ActionException if the command fails
	 */
	public Response execute(Command command, LightLevelProvider light) throws ActionException {
		// Verify command pre-requisites
		command.verify();

		// Check light is available
		final Entity actor = command.actor();
		final ActionDescriptor action = command.action();
		if(action.action().isFlag(Flag.LIGHT) && !light.isAvailable(actor.location())) throw REQUIRES_LIGHT;

		// Init power transaction
		final Transaction power = power(actor, action);

		// Inject actor argument
		final List<Object> args = new ArrayList<>();
		if(action.isActorRequired()) {
			args.add(actor);
		}

		// Inject enumeration constant
		action.constant().ifPresent(args::add);

		// Add command arguments
		args.addAll(command.arguments());

		// Lookup and inject required object arguments
		final var required = find(actor, action);
		required.entrySet().stream().filter(e -> e.getKey().isInjected()).map(Map.Entry::getValue).forEach(args::add);

		// Add optional effort argument
		if(action.isEffortAction()) {
			args.add(command.effort());
		}

		// Invoke action
		final Response response = invoke(action, args);

		// Consume power
		if(power != null) {
			power.complete();
		}

		// Apply wear
		required.values().forEach(WorldObject::use);

		// Reveal actor
		if(action.action().isFlag(Flag.REVEALS)) {
			reveal(actor);
		}

		return response;
	}

	/**
	 * Creates a power transaction using this action.
	 * @param actor		Actor
	 * @param cost		Power cost
	 * @return Power transaction or <tt>null</tt> if none
	 * @throws ActionException if the actor has insufficient power
	 */
	private static Transaction power(Entity actor, ActionDescriptor action) throws ActionException {
		final int power = action.action().power(actor);
		if(power == 0) {
			return null;
		}
		else {
			final Transaction tx = actor.model().values().transaction(EntityValue.POWER, power, "action.insufficient.power");
			tx.check();
			return tx;
		}
	}

	/**
	 * Finds required objects in the actors inventory.
	 * @param actor			Actor
	 * @param action		Action descriptor
	 * @return Required objects mapped by the associated descriptor
	 * @throws ActionException if an object is not present or is broken
	 */
	private static Map<RequiredDescriptor, WorldObject> find(Entity actor, ActionDescriptor action) throws ActionException {
		final Map<RequiredDescriptor, WorldObject> args = new StrictMap<>();
		for(RequiredDescriptor required : action.required()) {
			// Find required object in actors inventory
			final WorldObject obj = actor.contents().select(WorldObject.class)
				.filter(required.filter()) // TODO - from cat
				.findAny()
				.orElseThrow(() -> ActionException.of("action.required", required.category()));

			// Check tools
			if(obj.isBroken()) {
				throw new ActionException(new Description("action.required.broken", obj.name()));
			}

			// Add argument
			args.put(required, obj);
		}

		return args;
	}

	/**
	 * Invokes an action.
	 * @param action		Action descriptor
	 * @param args			Arguments
	 * @return Response
	 * @throws ActionException if the action fails
	 */
	private static Response invoke(ActionDescriptor action, List<Object> args) throws ActionException {
		try {
			return action.invoke(args);
		}
		catch(InvocationTargetException e) {
			if(e.getCause() instanceof ActionException) {
				throw (ActionException) e.getCause();
			}
			else {
				throw new RuntimeException(e);
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reveals a sneaking or hidden actor.
	 * @param actor Actor
	 */
	private static void reveal(Entity actor) {
		final Stance stance = actor.model().stance();
		if(stance.isVisibilityModifier() && (stance != Stance.DEFAULT)) {
			actor.model().stance(Stance.DEFAULT);
			actor.model().values().visibility().remove();
		}
	}
}
