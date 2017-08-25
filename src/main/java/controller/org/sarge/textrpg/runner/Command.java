package org.sarge.textrpg.runner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.object.EqualsBuilder;
import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.world.Location;

/**
 * Entity command.
 * @author Sarge
 */
public final class Command {
	private static final Logger LOG = Logger.getLogger(Command.class.getName());

	private final AbstractAction action;
	private final Method method;
	private final Object[] args;

	/**
	 * Constructor.
	 * @param action	Action
	 * @param method	Action method
	 * @param args		Zero-or-more action arguments
	 */
	public Command(AbstractAction action, Method method, Object[] args) {
		Check.notNull(action);
		Check.notNull(method);
		Check.notNull(args);
		this.action = action;
		this.method = method;
		this.args = args;
	}

	/**
	 * @return Action
	 */
	protected AbstractAction getAction() {
		return action;
	}

	/**
	 * @return Previous object
	 */
	public WorldObject getPreviousObject() {
		for(Object obj : args) {
			if(obj instanceof WorldObject) return (WorldObject) obj;
		}
		return null;
	}

	/**
	 * Executes this command.
	 * @param player		Player
	 * @param daylight		Whether ambient light is available
	 * @throws ActionException if the action fails
	 * @return Action response or <tt>null</tt> in the event of an error
	 */
	public ActionResponse execute(Player player, boolean daylight) throws ActionException {
		replacePreviousObject(player);
		verify(player, daylight);
		final Object[] args = build(player);
		final ActionResponse res = execute(args);
		updatePreviousObject(player);
		return res;
	}

	/**
	 * Replaces the previous object if used as an argument.
	 * @param player Player
	 */
	private void replacePreviousObject(Player player) {
		final WorldObject obj = player.getPreviousObject();
		if(obj == null) return;
		for(int n = 0; n < args.length; ++n) {
			if(args[n] == WorldObject.PREVIOUS) {
				args[n] = obj;
			}
		}
	}

	/**
	 * Updates the previous object for the given player.
	 * @param player Player
	 */
	private void updatePreviousObject(Player player) {
		final WorldObject obj = player.getPreviousObject();
		for (Object arg : args) {
			if((arg != obj) && (arg instanceof WorldObject)) {
				player.setPreviousObject((WorldObject) arg);
				break;
			}
		}
	}

	/**
	 * Pre-action verification.
	 * @param actor			Actor
	 * @param daylight		Whether ambient light is available
	 * @throws ActionException if the action cannot be performed
	 */
	private void verify(Entity actor, boolean daylight) throws ActionException {
		// Verify stance
		final Stance stance = actor.getStance();
		if((stance == Stance.COMBAT) && action.isCombatBlockedAction()) throw new ActionException("action.combat.blocked");
		if(!action.isValidStance(stance)) throw new ActionException("action.invalid." + stance);

		// Verify action can be performed in a parent
		final boolean remote = isRemoteArgument(actor);
		if(action.isParentBlockedAction()) {
			final String name = actor.getParent().getParentName();
			if(remote && !name.equals(Location.NAME)) throw new ActionException("action.invalid." + name, actor.getParent());
		}

		// Check for available light
		final Location loc = actor.getLocation();
		boolean light = daylight;
		if(action.isLightRequiredAction() && !light) {
			light = loc.isLightAvailable(light);
			if(!light) throw new ActionException("action.requires.light");
		}

		// Check whether can see non-inventory argument(s)
		if(!light && remote) {
			if(!loc.isArtificialLightAvailable()) throw new ActionException("action.unknown.argument");
		}

		// Reveal if sneaking
		if((stance == Stance.SNEAKING) && action.isVisibleAction()) {
			actor.setStance(Stance.DEFAULT);
		}
	}

	/**
	 * Tests whether command refers to remote a (non-inventory) argument.
	 */
	private boolean isRemoteArgument(Actor actor) {
		for(Object obj : args) {
			if(obj instanceof Thing) {
				final Thing thing = (Thing) obj;
				if(thing.getParent() != actor) return true;
			}
		}
		return false;
	}

	/**
	 * Builds full argument list.
	 */
	private Object[] build(Entity actor) {
		final int len = 1 + this.args.length;
		final Object[] args = new Object[len];
		args[0] = actor;
		System.arraycopy(this.args, 0, args, 2, this.args.length);
		return args;
	}

	/**
	 * Executes this action.
	 * @return Response
	 * @throws ActionException if the action failed
	 */
	private ActionResponse execute(Object[] args) throws ActionException {
		try {
			return (ActionResponse) method.invoke(action, args);
		}
		catch(InvocationTargetException e) {
			if(e.getTargetException() instanceof ActionException) {
				throw (ActionException) e.getTargetException();
			}
			else {
				LOG.log(Level.SEVERE, "Error executing action", e);
				return null;
			}
		}
		catch(Exception e) {
			LOG.log(Level.SEVERE, "Error invoking action", e);
			return null;
		}
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.equals(this, that);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
