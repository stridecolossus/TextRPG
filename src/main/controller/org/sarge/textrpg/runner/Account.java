package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notEmpty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Player account details.
 * <p>
 * An account is comprised of:
 * <ul>
 * <li>credentials</li>
 * <li>locale</li>
 * <li>list of player characters</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public final class Account extends AbstractEqualsObject {
	/**
	 * Player character summary.
	 */
	public static final class PlayerSummary extends AbstractEqualsObject {
		private final String name;
		private final Optional<LocalDateTime> last;

		/**
		 * Constructor.
		 * @param name Player name
		 * @param last Last-played
		 */
		private PlayerSummary(String name, LocalDateTime last) {
			this.name = notEmpty(name);
			this.last = Optional.of(last);
		}

		/**
		 * Constructor for a new player.
		 * @param name Player name
		 */
		private PlayerSummary(String name) {
			this.name = notEmpty(name);
			this.last = Optional.empty();
		}

		/**
		 * @return Player name
		 */
		public String name() {
			return name;
		}

		/**
		 * @return Last-played date or empty for a new player
		 */
		public Optional<LocalDateTime> last() {
			return last;
		}
	}

	// TODO
	// - locale
	// - remove player

	private final String name;
	private final List<PlayerSummary> players;

	/**
	 * Constructor for a new account.
	 * @param name Account name
	 */
	public Account(String name) {
		this(name, List.of());
	}

	/**
	 * Constructor.
	 * @param name 			Account name
	 * @param players		Player summaries
	 */
	public Account(String name, List<PlayerSummary> players) {
		this.name = notEmpty(name);
		this.players = new StrictList<>(players);
	}

	/**
	 * @return Account name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Player names in this account
	 */
	public List<PlayerSummary> players() {
		return players;
	}

	/**
	 * Adds a new player.
	 * @param player Player name
	 */
	void add(String player) {
		players.add(new PlayerSummary(player));
	}
}
