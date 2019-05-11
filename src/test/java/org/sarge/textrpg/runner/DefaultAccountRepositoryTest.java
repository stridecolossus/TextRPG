package org.sarge.textrpg.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultAccountRepositoryTest {
	private AccountRepository repository;

	@BeforeEach
	public void before() {
		repository = new DefaultAccountRepository();
	}

	@Test
	public void findNotPresent() {
		assertEquals(Optional.empty(), repository.find("name"));
	}

	@Test
	public void create() {
		final Account account = new Account("name");
		repository.create(account);
		assertEquals(Optional.of(account), repository.find("name"));
	}

	@Test
	public void createDuplicate() {
		final Account account = new Account("name");
		repository.create(account);
		assertThrows(IllegalStateException.class, () -> repository.create(account));
	}
}
