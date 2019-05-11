package org.sarge.textrpg.runner;

import java.util.Map;
import java.util.Optional;

import org.sarge.lib.collection.StrictMap;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation.
 * @author Sarge
 */
@Repository
public class DefaultAccountRepository implements AccountRepository {
	private final Map<String, Account> accounts = new StrictMap<>();
	private final Map<String, PlayerCharacter> players = new StrictMap<>();

	@Override
	public Optional<Account> find(String name) {
		return Optional.ofNullable(accounts.get(name));
	}

	@Override
	public void create(Account account) {
		if(accounts.containsKey(account.name())) throw new IllegalStateException("Duplicate account name: " + account);
		accounts.put(account.name(), account);
	}

	@Override
	public PlayerCharacter load(String name) {
		final PlayerCharacter pc = players.get(name);
		if(pc == null) throw new IllegalStateException("Unknown player: " + name);
		return pc;
	}

	@Override
	public void create(PlayerCharacter player) {
		if(accounts.containsKey(player.name())) throw new IllegalStateException("Duplicate player: " + player);
		players.put(player.name(), player);
	}
}
