package org.sarge.textrpg.runner;

import java.util.Optional;

import org.sarge.textrpg.entity.PlayerCharacter;

/**
 * Account repository.
 * @author Sarge
 */
public interface AccountRepository {
	/**
	 * Finds the account with the given name.
	 * @param name Account name
	 * @return Account
	 */
	Optional<Account> find(String name);

	/**
	 * Creates a new account.
	 * @param account New account
	 */
	void create(Account account);

	/**
	 * Loads a player character.
	 * @param name Character name
	 * @return Player
	 */
	PlayerCharacter load(String name);

	/**
	 * Creates a new player-character.
	 * @param player New character
	 */
	void create(PlayerCharacter player);

	// TODO
	// - delete
	// - update (modified players, locale)
}
