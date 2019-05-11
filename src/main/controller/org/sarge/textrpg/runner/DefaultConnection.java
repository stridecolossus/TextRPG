package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Socket-based connection implemented using a blocking thread for incoming client commands.
 * @author Sarge
 */
public class DefaultConnection extends AbstractConnection {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultConnection.class);

	private final Socket socket;
	private final PrintWriter out;

	/**
	 * Constructor.
	 * @param socket Underlying socket
	 * @throws IOException if the connection cannot be created
	 */
	public DefaultConnection(Socket socket) throws IOException {
		super(new BufferedReader(new InputStreamReader(socket.getInputStream())));
		this.socket = notNull(socket);
		this.out = new PrintWriter(socket.getOutputStream(), true);
	}

	@Override
	public void write(String str) {
		out.println(str);
	}

	@Override
	public synchronized void close() {
		// Stop command listener
		super.close();

		// Close underlying socket
		if(!socket.isClosed()) {
			try {
				socket.close();
			}
			catch(IOException e) {
				LOG.error("Error closing underlying socket: " + this, e);
			}
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("socket", socket).toString();
	}
}
