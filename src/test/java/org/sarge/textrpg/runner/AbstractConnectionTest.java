package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.runner.AbstractConnection.CommandReader;
import org.sarge.textrpg.runner.Connection.Listener;

public class AbstractConnectionTest {
	private static final String COMMAND = "command";

	private AbstractConnection con;
	private CommandReader in;
	private Listener listener;
	private Semaphore semaphore;

	@BeforeEach
	public void before() {
		// Create client reader
		semaphore = new Semaphore(1);
		in = new CommandReader() {
			@Override
			public String read() throws IOException {
				try {
					semaphore.acquire();
				}
				catch(InterruptedException e) {
					fail("Failed to acquire semaphore");
				}
				return COMMAND;
			}
		};

		// Create connection
		con = new AbstractConnection(in) {
			@Override
			public void write(String str) {
				throw new UnsupportedOperationException();
			}
		};
		listener = mock(Listener.class);
	}

	@AfterEach
	public void after() {
		con.close();
	}

	@Test
	public void read() throws IOException {
		con.start(listener);
		assertTimeout(Duration.ofSeconds(5), () -> {
			while(!semaphore.hasQueuedThreads()) {
				// Loop
			}
			verify(listener).handle(COMMAND);
		});
	}

	@Test
	public void startAlreadyStarted() {
		con.start(listener);
		assertThrows(IllegalStateException.class, () -> con.start(listener));
	}

	@Test
	public void close() {
		con.start(listener);
		con.close();
	}

	@Test
	public void connectionClosed() {
		con.start(listener);
		listener.closed();
	}
}
