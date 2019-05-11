package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Percentile;

/**
 * Set of tracks in a location.
 */
public final class Tracks extends AbstractEqualsObject {
	private final Location loc;
	private final String creator;
	private final Direction dir;
	private final long created;

	private transient Tracks next;
	private transient Tracks prev;

	private Percentile vis;

	/**
	 * Constructor.
	 * @param location		Location
	 * @param creator		Creator of these tracks
	 * @param dir			Direction
	 * @param vis			Initial visibility
	 * @param created		Creation time
	 * @param prev			Optional previous set of tracks
	 */
	public Tracks(Location location, String creator, Direction dir, Percentile vis, long created, Tracks prev) {
		this.loc = location;
		this.creator = notEmpty(creator);
		this.dir = notNull(dir);
		this.vis = notNull(vis);
		this.created = zeroOrMore(created);
		link(prev);
	}

	/**
	 * Links this set of tracks to the previous instance.
	 * @param prev Previous tracks
	 */
	private void link(Tracks prev) {
		if(prev != null) {
			this.prev = prev;
			prev.next = this;
		}
	}

	/**
	 * @return Creator of these tracks
	 */
	public String creator() {
		return creator;
	}

	/**
	 * @return Direction
	 */
	public Direction direction() {
		return dir;
	}

	/**
	 * @return Initial visibility of this set of tracks
	 */
	public Percentile visibility() {
		return vis;
	}

	/**
	 * @return Creation time
	 */
	public long created() {
		return created;
	}

	/**
	 * @return Next set of tracks in this trail or <tt>null</tt> if none
	 */
	Tracks next() {
		return next;
	}

	/**
	 * @return Previous set of tracks or <tt>null</tt> if none
	 */
	Tracks previous() {
		return prev;
	}

	/**
	 * Conceals these tracks.
	 * @param mod Visibility modifier
	 */
	void conceal(Percentile mod) {
		vis = vis.scale(mod);
	}

	/**
	 * Removes this set of tracks.
	 */
	void remove() {
		// Remove next link
		if(next != null) {
			next.prev = null;
			next = null;
		}

		// Remove previous link
		if(prev != null) {
			prev.next = null;
			prev = null;
		}

		// Remove tracks
		loc.remove(this);
	}
}