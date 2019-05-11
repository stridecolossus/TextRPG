package org.sarge.textrpg.world;

import java.util.Optional;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;

/**
 * A <i>link</i> is a descriptor for the properties of an {@link Exit}.
 * <p>
 * The various methods are intended to be over-ridden by sub-classes to customise link behaviour.
 * <p>
 * In particular:
 * <ul>
 * <li>{@link #controller()} is the associated controller object that determines link behaviour, e.g. a door</li>
 * <li>{@link #reason(Thing)} returns the reason code if a link cannot be traversed</li>
 * <li>{@link #isTraversable()} is used to control whether the exit is available for actions such as <i>flee</i></li>
 * <li>{@link #wrap(String)} renders the short exit description</li>
 * </ul>
 * @author Sarge
 */
public class Link extends AbstractEqualsObject {
	/**
	 * Descriptor for a simple link.
	 */
	public static final Link DEFAULT = new Link();

	/**
	 * Reason code for an invalid movement direction.
	 */
	public static final String INVALID_DIRECTION = "move.invalid.direction";

	/**
	 * Empty reason.
	 */
	private static final Optional<Description> EMPTY_REASON = Optional.empty();

	/**
	 * Empty traversal message.
	 */
	private static final Optional<Description> EMPTY_MESSAGE = Optional.empty();

	/**
	 * Empty controller.
	 */
	protected static final Optional<Thing> EMPTY_CONTROLLER = Optional.empty();

	/**
	 * Default constructor.
	 */
	protected Link() {
	}

	/**
	 * @return Size constraint of this link (default is {@link Size#NONE})
	 */
	public Size size() {
		return Size.NONE;
	}

	/**
	 * @return Route along this link (default is {@link Route#NONE})
	 */
	public Route route() {
		return Route.NONE;
	}

	/**
	 * @return Movement cost modifier (default is <tt>one</tt>)
	 */
	public float modifier() {
		return 1;
	}

	/**
	 * Over-ride to omit this link from location descriptions.
	 * @return Whether this link is quiet (default is <tt>false</tt>)
	 */
	public boolean isQuiet() {
		return false;
	}

	/**
	 * Over-ride to customise or disguise the destination name.
	 * @param dest Destination
	 * @return Destination name (default is the name of the given destination)
	 */
	public String name(Location dest) {
		return dest.name();
	}

	/**
	 * @return Controlling object for this link (default is none)
	 */
	public Optional<Thing> controller() {
		return EMPTY_CONTROLLER;
	}

	/**
	 * Over-ride to prevent this link being selected by actions, e.g. flee.
	 * @return Whether this link can be traversed (default is <tt>true</tt>)
	 */
	public boolean isTraversable() {
		return true;
	}

	/**
	 * @return Whether this link can only be traversed by entities, i.e. not vehicles or mounts (default is <tt>false</tt>)
	 */
	public boolean isEntityOnly() {
		return false;
	}

	/**
	 * @return Description key suffix for this link
	 */
	public String key() {
		return "default";
	}

	/**
	 * Tests whether the given actor can traverse this link.
	 * @param actor Entity or object attempting to traverse this link
	 * @return Reason code (default is none)
	 */
	public Optional<Description> reason(Thing actor) {
		return EMPTY_REASON;
	}

	/**
	 * @return Optional traversal message (default is none)
	 */
	public Optional<Description> message() {
		return EMPTY_MESSAGE;
	}

	/**
	 * Formats the direction summary of this link (default does nothing).
	 * @param dir Direction
	 * @return Formatted direction
	 */
	public String wrap(String dir) {
		return dir;
	}

	/**
	 * Adds additional description arguments for this link. The default implementation adds the controller name if present.
	 * @param description Description builder
	 */
	public void describe(Description.Builder description) {
		controller().map(Thing::name).ifPresent(description::name);
	}

	/**
	 * Inverts this link.
	 * @return Inverted link (returns <b>this</b> link by default)
	 */
	protected Link invert() {
		return this;
	}
}
