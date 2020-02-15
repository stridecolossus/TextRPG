package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * An <i>exit</i> describes a connection to another location.
 * @author Sarge
 */
public final class Exit extends AbstractEqualsObject {
	private final Direction dir;
	private final Link link;
	private final Location dest;

	/**
	 * Constructor.
	 * @param dir		Exit direction
	 * @param link		Link descriptor
	 * @param dest		Destination
	 */
	public Exit(Direction dir, Link link, Location dest) {
		this.dir = notNull(dir);
		this.link = notNull(link);
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
		return link;
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
		return link.controller().map(actor::perceives).orElse(true);
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append(dir);
		str.append("->");
		str.append(dest.name());
		if(link != Link.DEFAULT) {
			str.append('(');
			str.append(link);
			str.append(')');
		}
		return str.toString();
	}
}
