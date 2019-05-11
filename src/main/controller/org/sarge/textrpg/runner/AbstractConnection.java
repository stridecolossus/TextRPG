package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import java.io.BufferedReader;
import java.io.IOException;

import org.sarge.textrpg.util.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation using a blocking thread for the command listener.
 */
public abstract class AbstractConnection implements Connection {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractConnection.class);

	/**
	 * Adapter for a command reader.
	 */
	protected interface CommandReader {
		/**
		 * Reads the next command (blocking operation).
		 * @return Next command
		 * @throws IOException if the underlying reader has been closed
		 */
		String read() throws IOException;
	}

	private final CommandReader reader;

	private final Runner runner;

	private Listener listener;

	/**
	 * Constructor.
	 * @param in Client command stream
	 */
	protected AbstractConnection(BufferedReader in) {
		this(in::readLine);
	}

	/**
	 * Constructor.
	 * @param reader Command reader
	 */
	protected AbstractConnection(CommandReader reader) {
		this.reader = notNull(reader);
		runner = Runner.of(this::read);
	}

	@Override
	public void start(Listener listener) {
		if(this.listener != null) throw new IllegalStateException("Connection listener has already been started: " + this);
		this.listener = notNull(listener);
		runner.start();
	}

	@Override
	public void close() {
		if(runner.isRunning()) {
			runner.stop();
		}
	}

	/**
	 * Waits for next client command.
	 */
	private void read() {
		// Wait for next command
		final String command;
		try {
			command = reader.read();
		}
		catch(IOException e) {
			if(runner.isRunning()) {
				LOG.info("Socket closed by client: " + this, e);
				listener.closed();
			}
			return;
		}

		// Delegate to handler
		try {
			listener.handle(command);
		}
		catch(Exception e) {
			LOG.error("Uncaught exception in command handler: " + this, e);
		}
	}
}
