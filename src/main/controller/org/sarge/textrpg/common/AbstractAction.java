package org.sarge.textrpg.common;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

/**
 * Base-class for an action performed by an actor.
 * <p>
 * The default behaviour of an action can be customised as follows:
 * <ul>
 * <li>behaviour flags can be toggled in the constructor</li>
 * <li>the {@link #isValid(Terrain)} method specifies which terrain(s) are supported</li>
 * <li>{@link #isValid(Stance)} specifies what actor stances are valid</li>
 * <li>{@link #isInductionValid()} dictates whether the action can be performed when a <i>primary</i> induction is active</li>
 * </ul>
 * The {@link #verify(Entity)} applies the behavioural constraints.
 * <p>
 * @author Sarge
 */
public abstract class AbstractAction extends AbstractObject {
	/**
	 * Action effort.
	 */
	public enum Effort implements CommandArgument {
		QUICK,
		FAST,
		NORMAL,
		CAREFUL,
		THOROUGH
	}

	/**
	 * Behaviour flags for this action.
	 */
	public enum Flag {
		/**
		 * Requires an available light-source.
		 */
		LIGHT,

		/**
		 * Requires the actor to be <i>outside</i>, i.e. not inside something such as a vehicle or furniture.
		 * This is a default flag.
		 * @see Actor#parent()
		 */
		OUTSIDE,

		/**
		 * Automatically reveals a hidden or sneaking actor.
		 */
		REVEALS,

		/**
		 * Requires the actor to be active, i.e. not asleep.
		 * This is a default flag.
		 */
		ACTIVE,

		/**
		 * Whether this action starts an <i>active</i> induction.
		 * @see Induction
		 */
		INDUCTION,

		/**
		 * Broadcasts a notification of this action on completion.
		 */
		BROADCAST;

		/**
		 * @return Whether this is a default flag for all actions
		 */
		boolean isDefault() {
			switch(this) {
			case OUTSIDE:
			case ACTIVE:
				return true;

			default:
				return false;
			}
		}
	}

	private static final Set<Flag> DEFAULT = Arrays.stream(Flag.values()).filter(Flag::isDefault).collect(toSet());

	private final EnumSet<Flag> flags = EnumSet.copyOf(DEFAULT);

	/**
	 * Constructor.
	 * @param flags Over-ridden flags to toggle
	 */
	protected AbstractAction(Flag... flags) {
		for(Flag f : flags) {
			if(this.flags.contains(f)) {
				this.flags.remove(f);
			}
			else {
				this.flags.add(f);
			}
		}
	}

	/**
	 * @param f Flag
	 * @return Whether this action has the required flag
	 */
	public boolean isFlag(Flag f) {
		return flags.contains(f);
	}

	/**
	 * @return Action name prefix (default is none)
	 */
	public String prefix() {
		return StringUtils.EMPTY;
	}

	/**
	 * @param stance Stance
	 * @return Whether the given stance is valid for this action
	 * @see #isActive()
	 */
	protected boolean isValid(Stance stance) {
		switch(stance) {
		case MOUNTED:
		case RESTING:
		case SWIMMING:
			// TODO - can only be resting if all args are local
			return false;

		case SLEEPING:
			throw new UnsupportedOperationException("SLEEPING stance should be handled by the ACTIVE flag");

		default:
			return true;
		}
	}

	/**
	 * @param terrain Terrain
	 * @return Whether the given terrain is valid for this action (default is <tt>true</tt> except for {@link Terrain#WATER})
	 */
	protected boolean isValid(Terrain terrain) {
		return terrain != Terrain.WATER;
	}

	/**
	 * @param descriptor Current induction descriptor
	 * @return Whether this action can be performed when a <i>primary</i> induction is active (default is <tt>false</tt>)
	 */
	protected boolean isInductionValid() {
		return false;
	}

	/**
	 * Determines the power cost of this action.
	 * @param actor Actor
	 * @return Power cost (default is zero)
	 */
	public int power(Entity actor) {
		return 0;
	}

	/**
	 * Provides custom arguments parsers for this action.
	 * @param actor Actor
	 * @return Additional parsers for this action ordered by argument type (default is empty)
	 */
	public ArgumentParser.Registry parsers(Entity actor) {
		return ArgumentParser.Registry.EMPTY;
	}

	/**
	 * Verifies that the given actor satisfies the pre-requisites to execute this action.
	 * @param actor Actor
	 * @throws ActionException if a pre-requisite is not satisfied
	 */
	public void verify(Entity actor) throws ActionException {
		// Check stance
		final Stance stance = actor.model().stance();
		if(stance == Stance.SLEEPING) {
			if(isFlag(Flag.ACTIVE)) {
				throw ActionException.of("action.invalid.sleeping");
			}
		}
		else {
			if(!isValid(actor.model().stance())) {
				throw ActionException.of("action.invalid", stance.name());
			}
		}

		// Check terrain
		final Location loc = actor.location();
		if(!isValid(loc.terrain())) throw ActionException.of("action.invalid.terrain");

		// Check whether blocked by primary induction
		final Induction.Manager manager = actor.manager().induction();
		if(!isInductionValid() && manager.isPrimary()) {
			throw ActionException.of("action.invalid.primary");
		}

		// Check for active induction
		if(isFlag(Flag.INDUCTION) && manager.isActive()) {
			throw ActionException.of("action.invalid.induction");
		}

		// Check can be performed inside a parent
		final Parent parent = actor.parent();
		final boolean root = parent instanceof Location;
		if(!root && isFlag(Flag.OUTSIDE)) throw ActionException.of("action.invalid", parent.name());
	}

	/**
	 * Interrupts the active induction if any.
	 * @param actor Actor
	 */
	protected void interrupt(Entity actor) {
		final Induction.Manager manager = actor.manager().induction();
		if(manager.isActive()) {
			manager.interrupt();
		}
	}

	/**
	 * Determines whether the given skill attempt is successful.
	 * @param tier			Skill tier
	 * @param actor			Actor
	 * @param diff			Difficulty
	 * @return Whether successful
	 */
	protected boolean isSuccess(Entity actor, Skill skill, Percentile diff) {
		final Percentile score = actor.skills().find(skill).score();
		final double mod = skill.modifier().evaluate(actor.model());
		final double total = score.intValue() + mod;
		return diff.intValue() < total;
	}

	/**
	 * Helper - Creates a simple response with an argument.
	 * @param key		Key
	 * @param name		Argument
	 * @return Response
	 * @see Description#Description(String, Object)
	 */
	protected final static Response response(String key, String name) {
		return Response.of(new Description(key, name));
	}
}
