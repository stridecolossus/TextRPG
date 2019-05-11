package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.runner.Screen.ScreenException;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.world.Area;

public class SessionTest {
	private static final String COMMAND = "command";

	private Session session;
	private Connection connection;
	private Screen screen;

	@BeforeEach
	public void before() throws IOException {
		connection = mock(Connection.class);
		screen = mock(Screen.class);
		session = new Session(connection, screen);
		when(screen.handle(session, COMMAND)).thenReturn(screen);
	}

	@Test
	public void constructor() {
		assertEquals(null, session.player());
		assertEquals(null, session.account());
		assertEquals(null, session.store());
	}

	@Test
	public void write() {
		session.write(COMMAND);
		verify(connection).write(COMMAND);
	}

	@Test
	public void handle() {
		session.handle(COMMAND);
		verify(screen).handle(session, COMMAND);
	}

	@Test
	public void handleScreenTransition() {
		final Screen next = mock(Screen.class);
		when(screen.handle(session, COMMAND)).thenReturn(next);
		session.handle(COMMAND);
		verify(next).init(session);
	}

	@Test
	public void handleScreenException() {
		when(screen.handle(session, COMMAND)).thenThrow(new ScreenException("doh"));
		session.handle(COMMAND);
		verify(connection).write("doh");
	}

	@Test
	public void handleUncaughtException() {
		when(screen.handle(session, COMMAND)).thenThrow(new RuntimeException());
		session.handle(COMMAND);
		verifyNoMoreInteractions(connection);
	}

	@Test
	public void handleNullScreen() {
		when(screen.handle(session, COMMAND)).thenReturn(null);
		session.handle(COMMAND);
		verifyNoMoreInteractions(connection);
	}

	@Test
	public void close() {
		session.close();
		verify(connection).close();
	}

	@Test
	public void setPlayer() {
		final var player = mock(PlayerCharacter.class);
		session.set((PlayerCharacter) null);
		session.set(player);
		assertEquals(player, session.player());
	}

	@Test
	public void setAccount() {
		final Account account = new Account("name");
		session.set((Account) null);
		session.set(account);
		assertEquals(account, session.account());
	}

	@Test
	public void initNameStore() {
		final NameStore store = mock(NameStore.class);
		final Area area = new Area.Builder("area").store(store).build();
		session.init(area, store);
		assertEquals(store, session.store());
	}
}
