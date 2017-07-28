package org.sarge.textrpg.common;

/**
 * Notification.
 * @author Sarge
 */
public interface Notification {
	/**
	 * Handler for notifications.
	 */
	interface Handler {
		/**
		 * Default handler.
		 * @param n Notification
		 */
		void handle(Notification n);
		
		/**
		 * Handles a movement notification.
		 * @param move Movement notification
		 */
		void handle(MovementNotification move);
		
		/**
		 * Handles an environmental notification.
		 * @param env Notification
		 */
		void handle(EnvironmentNotification env);
		
		/**
		 * Handles a revealed object notification.
		 * @param reveal Notification
		 */
		void handle(RevealNotification reveal);
	}

	/**
	 * Dispatch this notification to the given handler.
	 * @param handler Notification handler
	 */
	void accept(Handler handler);

	/**
	 * Describes this notification.
	 * @return Description
	 */
	Description describe();
}
