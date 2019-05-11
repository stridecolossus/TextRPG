package org.sarge.textrpg.runner;

/**
 * A <i>connection</i> is the I/O link to a remote client.
 * @author Sarge
 */
public interface Connection {
	/**
	 * Handler for client events.
	 */
	interface Listener {
		/**
		 * Handles a client command string.
		 * @param command Command string
		 */
		void handle(String command);

		/**
		 * Notifies a remotely closed connection.
		 */
		void closed();
	}

	/**
	 * Starts this connection.
	 * @param listener Client event listener
	 */
	void start(Listener listener);

	/**
	 * Writes a message to this connection.
	 * @param str String to write
	 */
	void write(String str);

	/**
	 * Closes this connection.
	 */
	void close();
}
