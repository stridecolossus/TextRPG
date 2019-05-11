package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConnectionServerTest {
	private ConnectionServer server;
	private ConnectionServer.Handler handler;
	private Semaphore semaphore;

	@BeforeEach
	public void before() throws IOException {
		semaphore = new Semaphore(0);
		handler = con -> {
			assertNotNull(con);
			final Connection.Listener listener = mock(Connection.Listener.class);
			semaphore.release();
			return listener;
		};
		server = new ConnectionServer(1234, handler);
	}

	@AfterEach
	public void after() {
		server.stop();
	}

	@Test
	public void connect() throws Exception {
		server.start();
		try(final Socket client = new Socket("localhost", 1234)) {
			assertTimeout(Duration.ofSeconds(1), () -> semaphore.acquire());
		}
		assertEquals(0, semaphore.availablePermits());
	}
}
