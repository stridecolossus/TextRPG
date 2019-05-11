package org.sarge.textrpg.entity;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;

/**
 * A leader defines an actor that can lead other entities.
 * @see Follower
 * @author Sarge
 */
public interface Leader extends Actor {
	/**
	 * @return Leader model
	 */
	LeaderModel leader();

	/**
	 * Leader model.
	 */
	public static class LeaderModel extends AbstractEqualsObject {
		private final List<Follower> followers = new StrictList<>();

		/**
		 * @return Followers of this leader
		 */
		public Stream<Follower> followers() {
			return followers.stream();
		}

		/**
		 * Adds a follower
		 * @param follower Follower to add
		 */
		void add(Follower follower) {
			followers.add(follower);
		}

		/**
		 * Removes a follower
		 * @param follower Follower to remove
		 */
		void remove(Follower follower) {
			followers.remove(follower);
		}
	}
}
