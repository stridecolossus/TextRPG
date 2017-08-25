package org.sarge.textrpg.runner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConnectionServerTest {
    private static final int PORT = 1234;

    private ConnectionServer server;
    private Consumer<Socket> listener;

    @Before
    public void before() throws IOException {
        listener = mock(Consumer.class);
        server = new ConnectionServer(PORT, listener);
    }

    @After
    public void after() throws IOException {
        server.stop();
    }

    @Test
    public void connect() throws IOException {
        final Socket socket = new Socket("localhost", 1234);
        verify(listener).accept(socket);
        socket.close();
    }
}
