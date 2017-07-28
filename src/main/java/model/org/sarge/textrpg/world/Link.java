package org.sarge.textrpg.world;

import java.util.Optional;

import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;

/**
 * Link between locations.
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
	 * Over-ride for a custom route.
	 * @return Route-type of this link (default is {@link Route#NONE})
	 */
	public Route getRoute() {
		return Route.NONE;
	}

	/**
	 * Over-ride to customise the behaviour of this link.
	 * @return Controller for this link (default is none)
	 */
	public Optional<Thing> getController() {
		return DEFAULT_CONTROLLER;
	}

	/**
 	 * Over-ride to customise the size constraint of this link.
	 * @return Size constraint of this link (default is {@link Size#NONE})
	 */
	public Size getSize() {
		return Size.NONE;
	}

	/**
	 * Helper - Determines whether this link is visible to the given actor.
	 * @param exit		Exit
	 * @param actor		Actor
	 * @return Whether to display the exit
	 */
	public boolean isVisible(Actor actor) {
		final Optional<Thing> controller = getController();
		if(controller.isPresent()) {
			return controller.filter(Thing.NOT_QUIET).filter(actor::perceives).isPresent();
		}
		else {
			return true;
		}
	}

	/**
	 * Over-ride to customise whether this link can be traversed.
	 * @param actor Actor
	 * @return Whether this link can be traversed by the given actor (default is <tt>true</tt>)
	 * @see #getReason()
	 */
	public boolean isTraversable(Actor actor) {
		return true;
	}

	/**
	 * @return Reason code if this link cannot be traversed
	 * @see #isTraversable(Actor)
	 */
	public String getReason() {
		return "move.link.constraint";
	}

	/**
	 * @return Script to execute when traversing this script (default is {@link Script#NONE})
	 */
	public Script getScript() {
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
	 * Over-ride to change the name of the destination.
	 * @param dest Destination
	 * @return Destination name (default is the actual name)
	 * @see Location#getName()
	 */
	public String getDestinationName(Location dest) {
		return dest.getName();
	}

	/**
	 * Over-ride to customise the description of this link.
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
