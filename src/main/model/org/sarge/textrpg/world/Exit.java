package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * Descriptor for an exit from a location.
 * @author Sarge
 */
public class Exit extends AbstractEqualsObject {
	/**
	 * Creates a simple exit.
	 * @param dir		Direction
	 * @param dest		Destination
	 * @return Exit
	 * @see Link#DEFAULT
	 */
	public static final Exit of(Direction dir, Location dest) {
		return new Exit(dir, dest);
	}

	/**
	 * Creates an exit with a custom link.
	 * @param dir		Direction
	 * @param link		Link descriptor
	 * @param dest		Destination
	 * @return Exit
	 */
	public static final Exit of(Direction dir, Link link, Location dest) {
		Check.notNull(link);
		if(link == Link.DEFAULT) {
			return of(dir, dest);
		}
		else {
			return new Exit(dir, dest) {
				@Override
				public Link link() {
					return link;
				}
			};
		}
	}

	private final Direction dir;
	private final Location dest;

	/**
	 * Constructor.
	 * @param dir		Exit direction
	 * @param dest		Destination
	 */
	private Exit(Direction dir, Location dest) {
		this.dir = notNull(dir);
		this.dest = notNull(dest);
	}

	/**
	 * @return Exit direction
	 */
	public Direction direction() {
		return dir;
	}

	/**
	 * @return Link descriptor of this exit
	 */
	public Link link() {
		return Link.DEFAULT;
	}

	/**
	 * @return Destination of this exit
	 */
	public Location destination() {
		return dest;
	}

	/**
	 * Describes this exit for the given destination location.
	 * @return Exit description
	 * @see Link#describe(org.sarge.textrpg.util.Description.Builder)
	 */
	// TODO - partially visible exits, e.g. fog, dark, etc
	public Description describe() {
		// Build exit description key
		final Link link = link();
		final String key = TextHelper.join("location.exit", link.key());

		// Build exit description
		final var builder = new Description.Builder(key)
			.add("dir", TextHelper.prefix(dir))
			.add("dest", link.name(dest));

		// Add link details
		link.describe(builder);

		// Build description
		return builder.build();
	}

	/**
	 * Tests whether this exit is perceived by the given actor.
	 * @param actor Actor
	 * @return Whether this exit is perceived
	 */
	public boolean isPerceivedBy(Actor actor) {
		return link().controller().map(actor::perceives).orElse(true);
	}

	@Override
	public String toString() {
		// Build exit
		final StringBuilder str = new StringBuilder();
		str.append(dir);
		str.append("->");
		str.append(dest.name());

		// Add link descriptor
		final Link link = link();
		if(link != Link.DEFAULT) {
			str.append('(');
			str.append(link);
			str.append(')');
		}

		// Build result
		return str.toString();
	}
}
