package org.sarge.textrpg.runner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.Check;

/**
 * Handler for new socket connections.
 * @author Sarge
 */
public class ConnectionServer {
    private static final Logger LOG = Logger.getLogger(ConnectionServer.class.getName());

    private final RunnerAdapter runner;
    private final ServerSocket server;
    private final Consumer<Socket> listener;

    /**
     * Constructor.
     * @param port Connection port
     * @throws IOException if the socket server cannot be started
     */
    public ConnectionServer(int port, Consumer<Socket> listener) throws IOException {
        this.server = new ServerSocket(port);
        this.runner = new RunnerAdapter(this::run);
        this.listener = Check.notNull(listener);
    }

    /**
     * Accepts the next incoming connection.
     */
    private void run() {
        try {
            final Socket socket = server.accept();
            listener.accept(socket);
        }
        catch(SocketException e) {
            if(runner.isRunning()) {
                LOG.log(Level.SEVERE, "Socket error on accept", e);
            }
        }
        catch(Exception e) {
            LOG.log(Level.SEVERE, "Socket error on accept", e);
        }
    }

    /**
     * Stops this listener.
     * @throws IOException if the listener cannot be stopped
     * @throws IllegalArgumentException if already stopped
     */
    public void stop() throws IOException {
        runner.stop();
        server.close();
    }
}
