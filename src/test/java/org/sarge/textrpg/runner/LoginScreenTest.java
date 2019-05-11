package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginScreenTest {
	private LoginScreen screen;
	private AccountRepository repository;
	private Session session;

	@BeforeEach
	public void before() {
		session = mock(Session.class);
		repository = mock(AccountRepository.class);
		screen = new LoginScreen("welcome", repository);
	}

	@Test
	public void init() {
		screen.init(session);
		verify(session).write("welcome");
		assertEquals(null, session.account());
	}

	@Test
	public void handleAccountName() {
		assertEquals(screen, screen.handle(session, "name"));
		verify(session).set(new Account("name"));
	}

	@Test
	public void handleAccountCredentials() {
		final Screen next = mock(Screen.class);
		screen.setNext(next);
		final Account account = new Account("name");
		when(repository.find("name")).thenReturn(Optional.of(account));
		when(session.account()).thenReturn(account);
		assertEquals(next, screen.handle(session, "password"));
		verify(session).set(account);
	}

	@Test
	public void handleInvalidCredentials() {
		when(session.account()).thenReturn(new Account("name"));
		assertEquals(screen, screen.handle(session, "cobblers"));
		verify(session).write("Unknown account credentials");
	}

	@Test
	public void handleCreateAccount() {
		final Screen create = screen.handle(session, "new");
		// TODO
	}
}
