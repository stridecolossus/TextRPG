package org.sarge.textrpg.entity;

/**
 * Entity AI manager.
 * @author Sarge
 */
public interface EntityManager {
	/**
	 * Idle entity.
	 */
	EntityManager IDLE = new EntityManager() {
		@Override
		public void start(Entity entity) {
			// Does nowt
		}

		@Override
		public void stop(Entity entity) {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * Action performed on an entity by this manager.
	 */
	interface Action {
		/**
		 * Performs this action on the given entity.
		 * @param entity Entity
		 * @return Whether to terminate this action (when repeating)
		 */
		boolean execute(Entity entity);
	}

	/**
	 * Starts this entity manager.
	 * @param entity Entity
	 */
	void start(Entity entity);

	/**
	 * Stops this entity manager.
	 * @param entity Entity
	 */
	void stop(Entity entity);
}
