package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.runner.Account.PlayerSummary;

public class AccountTest {
	private Account account;

	@BeforeEach
	public void before() {
		account = new Account("name");
	}

	@Test
	public void constructor() {
		assertEquals("name", account.name());
		assertNotNull(account.players());
		assertEquals(0, account.players().size());
	}

	@Test
	public void add() {
		account.add("player");
		assertEquals(1, account.players().size());
		final PlayerSummary player = account.players().iterator().next();
		assertEquals("player", player.name());
		assertEquals(Optional.empty(), player.last());
	}
}
