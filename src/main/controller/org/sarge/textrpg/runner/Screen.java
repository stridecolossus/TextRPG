package org.sarge.textrpg.runner;

/**
 * Handler for a <i>screen</i>.
 * @author Sarge
 */
public interface Screen {
	/**
	 * Initialises this screen for the given session.
	 * @param session Session
	 */
	void init(Session session);

	/**
	 * Handle a command string.
	 * @param session		Session
	 * @param command		Command string
	 * @return Next screen
	 * @throws ScreenException if the command cannot be handled
	 */
	Screen handle(Session session, String command) throws ScreenException;

	/**
	 * Screen exception.
	 */
	static class ScreenException extends RuntimeException {
		public ScreenException(String message) {
			super(message);
		}
	}
}
