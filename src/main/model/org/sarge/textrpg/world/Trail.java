package org.sarge.textrpg.world;

import java.util.Deque;
import java.util.LinkedList;

import org.sarge.lib.util.AbstractObject;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.util.Percentile;

/**
 * A <i>trail</i> is the list of {@link Tracks} generated by an entity or vehicle.
 * @see Location#add(String, Direction, Percentile, long, Tracks)
 * @author Sarge
 */
public class Trail extends AbstractObject {
	/**
	 * Trail segment.
	 */
	private static class Segment extends AbstractObject {
		private Tracks start;
		private Tracks prev;

		/**
		 * Adds a set of tracks.
		 * @param tracks Tracks to add
		 */
		private void add(Tracks tracks) {
			if(start == null) {
				start = tracks;
			}

			prev = tracks;
		}

		/**
		 * Prunes expired tracks in this segment.
		 * @param expiry Expiry time
		 * @return Whether this segment was completely pruned
		 */
		private boolean prune(long expiry) {
			while(start.created() < expiry) {
				// Walk to next tracks in this segment
				final Tracks removed = start;
				start = start.next();

				// Remove expired tracks
				removed.remove();

				// Stop if segment completely pruned
				if(start == null) {
					return true;
				}
			}

			// Segment partially pruned
			return false;
		}

		/**
		 * Removes all tracks in this segment.
		 */
		private void clear() {
			while(true) {
				// Stop if no more tracks
				if(start == null) {
					break;
				}

				// Walk to next tracks
				final Tracks t = start;
				start = start.next();

				// Remove tracks
				t.remove();
			}
		}
	}

	private final Deque<Segment> segments = new LinkedList<>();

	/**
	 * Constructor.
	 */
	public Trail() {
		push();
	}

	/**
	 * @return Previous set of tracks in this trail or <tt>null</tt> if none
	 */
	public Tracks previous() {
		final Segment current = segments.peek();
		return current.prev;
	}

	/**
	 * Adds a set of tracks to this trail.
	 * @param tracks Tracks to add
	 */
	void add(Tracks tracks) {
		Check.notNull(tracks);
		final Segment current = segments.peek();
		current.add(tracks);
	}

	/**
	 * Stops the current segment, e.g. when an entity enters a vehicle or starts swimming.
	 */
	void stop() {
		push();
	}

	/**
	 * Pushes a new segment.
	 */
	private void push() {
		segments.push(new Segment());
	}

	/**
	 * Prunes expired tracks from this trail.
	 * @param expiry Expiry time
	 */
	void prune(long expiry) {
		Check.zeroOrMore(expiry);

		// Check for empty tracks
		if((segments.size() == 1) && (segments.peek().start == null)) {
			return;
		}

		// Prune segments
		while(true) {
			// Prune oldest segment
			final Segment segment = segments.getLast();
			final boolean pruned = segment.prune(expiry);

			if(pruned) {
				// Remove pruned segment
				segments.removeLast();

				// Stop if all tracks removed
				if(segments.isEmpty()) {
					push();
					break;
				}
			}
			else {
				// Stop if reached active tracks
				break;
			}
		}
	}

	/**
	 * Clears this trail.
	 */
	public void clear() {
		segments.forEach(Segment::clear);
		segments.clear();
	}
}
