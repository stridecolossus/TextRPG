package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.sarge.textrpg.util.Runner;
import org.sarge.textrpg.util.ServiceComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The <i>connection server</i> listens for remote socket connections.
 * @see Connection
 * @author Sarge
 */
@Component
public class ConnectionServer extends Runner implements ServiceComponent {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionServer.class);

	/**
	 * Handler for new connections.
	 */
	interface Handler {
		/**
		 * Handles a new connection.
		 * @param con Connection
		 * @return Listener for client commands
		 */
		Connection.Listener handle(Connection con);
	}

	private final ServerSocket server;
	private final Handler handler;

	/**
	 * Constructor.
	 * @param port 			Port number
	 * @param handler		Handler for new connections
	 * @throws IOException if this server cannot be created
	 */
	public ConnectionServer(@Value("${server.port}") int port, Handler handler) throws IOException {
		this.server = new ServerSocket(port);
		this.handler = notNull(handler);
	}

	@Override
	public void start() {
		LOG.info("Starting connection server on port " + server.getLocalPort());
		super.start();
	}

	/**
	 * Stops this server.
	 */
	@Override
	public void stop() {
		LOG.info("Stopping connection server...");

		// Ignore if already stopped
		if(!isRunning()) {
			return;
		}

		// Stop thread
		super.stop();

		// Stop server
		try {
			server.close();
		}
		catch(IOException e) {
			LOG.error("Error closing server", e);
		}

		LOG.info("Stopped connection server");
	}

	@SuppressWarnings("resource")
	@Override
	protected void execute() {
		// Wait for next connection
		final Socket client;
		try {
			client = server.accept();
			LOG.info("New connection: " + client.getRemoteSocketAddress());
		}
		catch(Exception e) {
			if(isRunning()) {
				LOG.error("Error waiting for client connection", e);
				return;
			}
			else {
				// Ignore errors during shutdown
				return;
			}
		}

		// Create new connection
		try {
			final DefaultConnection con = new DefaultConnection(client);
			final Connection.Listener listener = handler.handle(con);
			con.start(listener);
		}
		catch(Exception e) {
			LOG.error("Error creating new connection", e);
		}
	}
}
