package org.sarge.textrpg.common;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.WorldObject;

/**
 * Base-class for an action performed by an {@link Entity}.
 * @author Sarge
 */
public abstract class AbstractAction {
	/**
	 * Response for an invalid action (syntactically incorrect).
	 */
	protected static final ActionResponse INVALID = new ActionResponse("action.invalid");

	/**
	 * Response code for an action that is syntactically correct but illogical.
	 */
	protected static final String ILLOGICAL = "action.illogical";

	/**
	 * Keyword filter for <b>all</b> contents.
	 */
	public static final String ALL = "all";

	protected final String name;

	private final EnumSet<Stance> stances;

	/**
	 * Default constructor.
	 */
	protected AbstractAction() {
		this(null);
	}

	/**
	 * Constructor.
	 * @param name Action name or <tt>null</tt> to use class-name
	 */
	protected AbstractAction(String name) {
		// Build action identifier
		if(name == null) {
			this.name = this.getClass().getSimpleName().replaceFirst("Action", "").toLowerCase();
		}
		else {
			this.name = name.toLowerCase();
		}

		// Enumerate valid stances
		this.stances = EnumSet.of(Stance.DEFAULT, Stance.RESTING, Stance.MOUNTED, Stance.SNEAKING);
		this.stances.removeAll(Arrays.asList(getInvalidStances()));
	}

	/**
	 * @return Action identifier
	 */
	public final String getName() {
		return "action." + name.toLowerCase();
	}

	/**
	 * Over-ride for actions that can be performed in-combat.
	 * @return Whether this action is blocked by being in combat (default is <tt>true</tt>)
	 */
	public boolean isCombatBlockedAction() {
		return true;
	}

	/**
	 * @return Whether this action requires light (default is <tt>true</tt>)
	 */
	public boolean isLightRequiredAction() {
		return true;
	}

	/**
	 * Over-ride for actions that do not affect the actors visibility.
	 * @return Whether this is a visible action that will reveal a hidden or sneaking actor (default is <tt>false</tt>)
	 */
	public boolean isVisibleAction() {
		return false;
	}

	/**
	 * Over-ride for custom stance validation.
	 * @return Invalid stances for this action (default is <b>any</b> stance except {@link Stance#COMBAT} and {@link Stance#SLEEPING})
	 */
	protected Stance[] getInvalidStances() {
		return new Stance[]{};
	}

	/**
	 * Tests whether the given stance is valid for this action.
	 * @param stance Stance
	 * @return Whether the given stance is valid
	 * @see #getInvalidStances()
	 */
	public final boolean isValidStance(Stance stance) {
		return stances.contains(stance);
	}

	/**
	 * Over-ride to allow this action to be performed when the actor is in something.
	 * @return Whether this action is blocked when the actor is in something, default is <tt>true</tt>
	 * @see Entity#getParent()
	 */
	public boolean isParentBlockedAction() {
		return true;
	}

	/**
	 * Helper - Verifies that the given object is carried by the actor.
	 * @param actor		Actor
	 * @param obj		Object
	 * @throws ActionException if the object is not carried
	 */
	protected static void verifyCarried(Actor actor, WorldObject obj) throws ActionException {
		if(obj.getOwner() != actor) throw new ActionException("action.not.carried");
	}

	/**
	 * Default handler for an invalid action in this location.
	 * @param actor Actor
	 * @return Invalid response
	 */
	public ActionResponse execute(Entity actor) throws ActionException {
		return INVALID;
	}

	/**
	 * Default handler for an invalid action on an object.
	 * @param actor		Actor
	 * @param obj		Object
	 * @return Illogical response
	 */
	public ActionResponse execute(Entity actor, Thing obj) throws ActionException {
		return new ActionResponse(new Description(ILLOGICAL, "name", obj));
	}

	/**
	 * Determines the duration for an inducted action.
	 * @param base		Base duration (ms)
	 * @param level		Associated skill level
	 * @return Modified duration (ms)
	 */
	protected static long calculateDuration(long base, int level) {
		Check.oneOrMore(level);
		if(level == 1) {
			return base;
		}
		else {
			final float mod = 1f - (2 * level) / 100f;
			return (long) (base * mod);
		}
	}

	/**
	 * Helper - Looks up the given skill level for an entity.
	 * @param actor Actor
	 * @param skill Skill descriptor
	 * @return Skill level
	 * @throws ActionException if the actor does not possess the given skill
	 * @see Entity#getSkillLevel(Skill)
	 */
	protected final int getSkillLevel(Entity actor, Skill skill) throws ActionException {
		return actor.getSkillLevel(skill).orElseThrow(() -> new ActionException(name + ".requires.skill"));
	}

	/**
	 * Helper - Finds an optional object in the given inventory.
	 * @param inv			Inventory
	 * @param matcher		Matcher
	 * @param recurse		Whether to recurse to containers
	 * @return Matched object if found
	 */
	protected final Optional<WorldObject> find(Actor actor, Predicate<WorldObject> matcher, boolean recurse) {
		return actor.getContents().stream(recurse ? Integer.MAX_VALUE : 0)
			.filter(t -> t instanceof WorldObject)
			.map(t -> (WorldObject) t)
			.filter(matcher)
			.findAny();
	}

	/**
	 * Helper - Finds a required object in the given inventory.
	 * @param inv		Inventory
	 * @param matcher	Matcher
	 * @param recurse	Whether to recurse to containers
	 * @param name		Object name
	 * @return Matched object if found
	 * @throws ActionException if the actor does not possess the given object or it is broken
	 * @see Entity#getContents()
	 */
	protected final WorldObject find(Actor actor, Predicate<WorldObject> matcher, boolean recurse, String name) throws ActionException {
		final WorldObject obj = find(actor, matcher, recurse).orElseThrow(() -> new ActionException(this.name + ".requires." + name));
		if(obj.isBroken()) throw new ActionException(this.name + ".broken." + name);
		return obj;
	}

	/**
	 * Helper - Creates a default response for this action with the given argument.
	 * @param arg Argument
	 * @return Response
	 */
	protected final ActionResponse response(Object arg) {
		return new ActionResponse(new Description(this.name + ".response", "name", arg.toString()));
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
