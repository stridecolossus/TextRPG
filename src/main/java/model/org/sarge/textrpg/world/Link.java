package org.sarge.textrpg.world;

import java.util.Optional;

import org.sarge.lib.object.ToString;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;

/**
 * Descriptor for a link between two locations.
 * <p>
 * A link specifies whether a given actor can move to a destination location depending on whether they perceive the link, the size of the actor, and the <i>controller</i> of the link (such as a portal).
 * <p>
 * Specialized link implementations should over-ride one or more of the following:
 * <ul>
 * <li>{@link #reason(Actor)} contains the logic to determine whether the link can be traversed</li>
 * <li>{@link #controller()} returns the controller for the link which determines whether the link is visible and/or open</li>
 * <li>{@link #size()} specifies the maximum size of an actor that can fit through this link</li>
 * <li>{@link #route()} is the route-type of the link</li>
 * <li>{@link #describe()} describes the link</li>
 * <li>additionally {@link #destinationName(Location)} can override the name of the destination</li>
 * </ul>
 * @author Sarge
 */
public class Link {
	/**
	 * Default controller.
	 */
	private static final Optional<Thing> DEFAULT_CONTROLLER = Optional.empty();

	/**
	 * Default (simple) link.
	 */
	public static final Link DEFAULT = new Link();

	/**
	 * Constructor.
	 */
	protected Link() {
	}

	/**
	 * @return Route-type of this link (default is {@link Route#NONE})
	 */
	public Route route() {
		return Route.NONE;
	}

	/**
	 * @return Controller for this link (default is none)
	 */
	public Optional<Thing> controller() {
		return DEFAULT_CONTROLLER;
	}

	/**
	 * @return Size constraint of this link (default is {@link Size#NONE})
	 */
	public Size size() {
		return Size.NONE;
	}

	/**
	 * Helper - Determines whether this link is visible to the given actor.
	 * @param exit		Exit
	 * @param actor		Actor
	 * @return Whether to display the exit
	 */
	public boolean isVisible(Actor actor) {
	    return controller()
	        .filter(Thing.NOT_QUIET)
	        .map(actor::perceives)
	        .orElse(true);
	}

	/**
	 * @param actor Actor
	 * @return Whether this link can be traversed by the given actor (default is <tt>true</tt>)
	 * @see #reason()
	 */
	public final boolean isTraversable(Actor actor) {
		return reason(actor) == null;
	}

	/**
	 * @return Reason code if this link cannot be traversed
	 * @see #isTraversable(Actor)
	 */
	public String reason(Actor actor) {
	    return null;
	}

	/**
	 * @return Script to execute when traversing this script (default is {@link Script#NONE})
	 */
	public Script script() {
		return Script.NONE;
	}

	/**
	 * Generates the short description of this link.
	 * @param dir Direction
	 * @returns Link description
	 */
	public String describe(String dir) {
		return dir;
	}

	/**
	 * @param dest Destination
	 * @return Destination name (default is the name of the destination location)
	 * @see Location#getName()
	 */
	public String destinationName(Location dest) {
		return dest.getName();
	}

	/**
	 * @return Long description of this link
	 */
	public Description.Builder describe() {
		return new Description.Builder("exit.entry");
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
