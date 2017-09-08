package org.sarge.textrpg.common;

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
 * TODO
 * - factor out Action interface?
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

	protected final String name;

	/**
	 * Default constructor.
	 */
	protected AbstractAction() {
        this.name = "action." + this.getClass().getSimpleName().replaceFirst("Action", "").toLowerCase();
	}

	/**
	 * Constructor.
	 * @param name Action name
	 */
	protected AbstractAction(String name) {
	    Check.notEmpty(name);
		this.name = "action." + name.toLowerCase();
	}

	/**
	 * @return Action identifier
	 */
	public final String getName() {
		return name;
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
	 * Tests whether the given stance is valid for this action.
	 * @param stance Stance
	 * @return Whether the given stance is valid
	 * @see #getInvalidStances()
	 */
	public boolean isValidStance(Stance stance) {
	    switch(stance) {
	        case DEFAULT:
	        case RESTING:
	        case MOUNTED:
	        case SNEAKING:
	            return true;

            default:
                return false;
	    }
	}

	/**
	 * Over-ride to allow this action to be performed when the actor is in something.
	 * @return Whether this action is blocked when the actor is in something, default is <tt>true</tt>
	 * @see Entity#parent()
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
		if(obj.owner() != actor) throw new ActionException("action.not.carried");
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
	 * @see Entity#skillLevel(Skill)
	 */
	protected final int getSkillLevel(Entity actor, Skill skill) throws ActionException {
		return actor.skillLevel(skill).orElseThrow(() -> new ActionException(name + ".requires.skill"));
	}

	/**
	 * Helper - Finds an optional object in the given inventory.
	 * @param inv			Inventory
	 * @param matcher		Matcher
	 * @param recurse		Whether to recurse to containers
	 * @return Matched object if found
	 */
	protected final Optional<WorldObject> find(Actor actor, Predicate<WorldObject> matcher, boolean recurse) {
		return actor.contents().stream(recurse ? Integer.MAX_VALUE : 0)
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
	 * @see Entity#contents()
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
