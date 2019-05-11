package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.runner.ActionDescriptor;
import org.sarge.textrpg.runner.ActionDescriptor.ActionParameter;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;

/**
 * A <i>command</i> is an action being performed by an actor.
 * @author Sarge
 */
public class Command extends AbstractEqualsObject {
	private final Entity actor;
	private final ActionDescriptor action;
	private final List<Object> args;
	private final AbstractAction.Effort effort;

	/**
	 * Constructor.
	 * @param actor			Actor
	 * @param action		Action descriptor
	 * @param args			Arguments
	 * @param effort		Effort argument
	 */
	public Command(Entity actor, ActionDescriptor action, List<Object> args, AbstractAction.Effort effort) {
		this.actor = notNull(actor);
		this.action = notNull(action);
		this.args = List.copyOf(args);
		this.effort = notNull(effort);
		verifyArguments();
	}

	/**
	 * Verifies the given arguments are valid for the action method.
	 */
	private void verifyArguments() {
		final List<ActionParameter> params = action.parameters();
		for(int n = 0; n < args.size(); ++n) {
			final Object obj = args.get(n);
			final Class<?> type = obj.getClass();
			if(!params.get(n).type().isAssignableFrom(type)) {
				throw new IllegalArgumentException(String.format("Invalid argument for action: index=%s expected=%s actual=%s", n, params.get(n), type));
			}
		}
	}

	/**
	 * @return Actor
	 */
	public Entity actor() {
		return actor;
	}

	/**
	 * @return Action descriptor
	 */
	public ActionDescriptor action() {
		return action;
	}

	/**
	 * @return Command arguments
	 */
	public List<Object> arguments() {
		return args;
	}

	/**
	 * @return Action effort
	 */
	public AbstractAction.Effort effort() {
		return effort;
	}

	/**
	 * Verifies that this command can be performed.
	 * @throws ActionException if the command cannot be performed
	 */
	public void verify() throws ActionException {
		action.action().verify(actor);
		verifyCarried();
	}

	/**
	 * Enforces command arguments that must be carried.
	 * @see Carried
	 * @throws ActionException if an argument is not carried
	 */
	private void verifyCarried() throws ActionException {
		final List<ActionParameter> params = action.parameters();
		for(int n = 0; n < args.size(); ++n) {
			// Ignore if argument does not need to be carried
			final var annotation = params.get(n).carried();
			if(!annotation.isPresent()) continue;

			// Check whether carried
			final WorldObject obj = (WorldObject) args.get(n);
			if(actor.contents().contains(obj)) continue;

			// Try to auto-take the object
			if(annotation.get().auto()) {
				// TODO
			}

			// Otherwise object is not carried
			throw new ActionException(new Description("object.not.carried", obj.name()));
		}
	}
}
