package org.sarge.textrpg.entity;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;

/**
 * A follower is an entity that can follow a {@link Leader}.
 * @author Sarge
 */
public interface Follower extends Actor {
	/**
	 * @return Follower model
	 */
	FollowerModel follower();

	/**
	 * Starts following the given leader.
	 * @param follower		Follower
	 * @param leader		Leader to follow
	 * @throws IllegalArgumentException if already following
	 */
	static void follow(Follower follower, Leader leader) {
		final FollowerModel model = follower.follower();
		if(model.leader != null) throw new IllegalArgumentException("Already following");
		leader.leader().add(follower);
		model.leader = leader;
	}

	/**
	 * Stops following the current leader.
	 * @param follower Follower to stop
	 * @return Previous leader
	 * @throws IllegalArgumentException if not following
	 */
	static Leader stop(Follower follower) {
		// Check following
		final FollowerModel model = follower.follower();
		if(!model.isFollowing()) throw new IllegalArgumentException("Not following");

		// Stop following
		final Leader prev = model.leader;
		model.stop(follower);

		return prev;
	}

	/**
	 * Clears the following state of the given follower.
	 * @param follower Follower
	 */
	static void clear(Follower follower) {
		final FollowerModel model = follower.follower();
		if(model.isFollowing()) {
			model.stop(follower);
		}
	}

	/**
	 * Model for a follower.
	 */
	public static class FollowerModel extends AbstractEqualsObject {
		private Leader leader;

		/**
		 * @param leader Leader
		 * @return Whether following the given leader
		 */
		public boolean isFollowing(Leader leader) {
			return this.leader == leader;
		}

		/**
		 * @return Whether following
		 */
		public boolean isFollowing() {
			return leader != null;
		}

		/**
		 * Stops following.
		 * @param follower Follower
		 */
		private void stop(Follower follower) {
			leader.leader().remove(follower);
			leader = null;
		}
	}
}
