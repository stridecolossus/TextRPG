package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.runner.Screen.ScreenException;

public class AccountManagementScreenTest {
	private AccountManagementScreen screen;
	private AccountRepository repository;
	private Session session;
	private Account account;

	@BeforeEach
	public void before() {
		repository = mock(AccountRepository.class);
		screen = new AccountManagementScreen(repository);
		account = new Account("account");
		session = mock(Session.class);
		when(session.account()).thenReturn(account);
	}

	@Test
	public void invalidCommand() {
		assertThrows(ScreenException.class, () -> screen.handle(session, "cobblers"));
	}

	@Test
	public void invalidCommandLength() {
		assertThrows(ScreenException.class, () -> screen.handle(session, "one two three"));
	}

	@Test
	public void list() {
		account.add("name");
		assertEquals(screen, screen.handle(session, "list"));
		verify(session).write("Players:");
		verify(session).write("name");
	}

	@Test
	public void play() {
		final Screen play = mock(Screen.class);
		screen.setPlay(play);
		final PlayerCharacter pc = mock(PlayerCharacter.class);
		when(repository.load("name")).thenReturn(pc);
		account.add("name");
		screen.handle(session, "play name");
		verify(session).set(pc);
	}

	@Test
	public void playUnknownPlayer() {
		assertThrows(ScreenException.class, () -> screen.handle(session, "player cobblers"));
	}

	// TODO
	// - create player
	// - delete player
	// - delete account
	// - quit
}
