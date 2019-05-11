package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.PlayerCharacter;

public class SessionManagerTest {
	private SessionManager manager;
	private Session session;
	private Screen start;

	@BeforeEach
	public void before() {
		start = mock(Screen.class);
		manager = new SessionManager(start);
		session = mock(Session.class);
	}

	@Test
	public void constructor() {
		assertEquals(0, manager.size());
		assertNotNull(manager.players());
	}

	@Test
	public void players() {
		final PlayerCharacter pc = mock(PlayerCharacter.class);
		when(session.player()).thenReturn(pc);
		manager.add(session);
		assertEquals(1, manager.players().count());
		assertEquals(pc, manager.players().iterator().next());
	}

	@Test
	public void add() {
		// Add session
		final Connection.Listener listener = manager.add(session);
		assertNotNull(listener);
		assertEquals(1, manager.size());

		// Check listener
		final String command = "command";
		listener.handle(command);
		verify(session).handle(command);

		// Close session remotely
		listener.closed();
		verify(session).close();
		assertEquals(0, manager.size());
	}

	@Test
	public void handle() {
		final Connection con = mock(Connection.class);
		final Connection.Listener listener = manager.handle(con);
		assertNotNull(listener);
		assertEquals(1, manager.size());
	}

	@Test
	public void close() {
		manager.add(session);
		manager.close(session);
		verify(session).close();
		assertEquals(0, manager.size());
	}

	@Test
	public void closeAll() {
		manager.add(session);
		manager.close();
		verify(session).close();
		assertEquals(0, manager.size());
	}
}
