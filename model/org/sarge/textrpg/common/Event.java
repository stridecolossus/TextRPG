package org.sarge.textrpg.common;

/**
 * Event that is executed at some point in the future.
 * @author Sarge
 */
@FunctionalInterface
public interface Event {
	/**
	 * Executes this event when it has expired.
	 */
	void execute();

	/**
	 * Holder for a pending event.
	 * <p>
	 * A holder is a helper used to manage an event that may be cancelled in the future, e.g. a decay event for food which is redundant if the food is eaten or destroyed.
	 * <p>
	 * Usage:
	 * <pre>
	 *   // Register an event
	 *   final Event event = ...
	 *   queue.add(event);
	 *   holder.set(event);
	 *   ...
	 *   // Event is superceeded, previous is cancelled
	 *   final Event event = ...
	 *   queue.add(event);
	 *   holder.set(event);
	 *   ...
	 *   // Event is redundant
	 *   holder.cancel();
	 * </pre>
	 * 
	 */
	class Holder {
		private EventQueue.Entry event;

		/**
		 * Registers an event and cancels the previous if active.
		 * @param event Event
		 * @see EventQueue.Entry#cancel()
		 */
		public void set(EventQueue.Entry event) {
			cancel();
			this.event = event;
		}
		
		/**
		 * Cancels the previous event if active.
		 */
		public void cancel() {
			if((event != null) && !event.isCancelled()) {
				event.cancel();
			}
		}
		
		@Override
		public String toString() {
			return event.toString();
		}
	}
}
