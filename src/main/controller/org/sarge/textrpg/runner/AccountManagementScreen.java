package org.sarge.textrpg.runner;

import static java.util.stream.Collectors.joining;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.runner.Account.PlayerSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Account management screen.
 * @author Sarge
 */
@Component("screen.account")
public class AccountManagementScreen implements Screen {
	private final AccountRepository repository;

	private Screen play;

	/**
	 * Constructor.
	 * @param repository 	Account repository
	 * @param play			Play screen
	 */
	public AccountManagementScreen(AccountRepository repository) {
		this.repository = notNull(repository);
	}

	/**
	 * Sets the play screen.
	 * @param play Play screen
	 */
	public void setPlay(@Qualifier("screen.play") Screen play) {
		this.play = play;
	}

	@Override
	public void init(Session session) {
		list(session);
	}

	@Override
	public Screen handle(Session session, String line) {
		// Tokenize line
		final String[] tokens = line.split(" ");
		final String command = tokens[0].trim();

		// Handle command
		switch(tokens.length) {
		case 1:
			return handle(command, session);

		case 2:
			final String name = tokens[1].trim();
			return handle(command, name, session);

		default:
			throw new ScreenException("Invalid command");
		}
	}

	/**
	 * Lists players.
	 * @param session Session
	 */
	private static void list(Session session) {
		final List<Account.PlayerSummary> players = session.account().players();
		if(players.isEmpty()) {
			session.write("No players!");
		}
		else {
			// TODO - date formatting, table layout?
			final String list = players.stream().map(AccountManagementScreen::list).collect(joining("\n"));
			session.write("Players:");
			session.write(list);
		}
	}

	/**
	 * Lists a player summary.
	 */
	private static String list(PlayerSummary player) {
		final StringBuilder sb = new StringBuilder();
		sb.append(player.name());
		if(player.last().isPresent()) {
			sb.append(" - ");
			sb.append(player.last().get()); // TODO - formatting
		}
		return sb.toString();
	}

	/**
	 * Handles an account command.
	 * @param command Command
	 * @param session Session
	 * @return Next screen
	 */
	private Screen handle(String command, Session session) {
		switch(command) {
		case "list":
			// List players
			list(session);
			return this;

		case "create":
			// Create new player
			// TODO
			// - return new CreatePlayerScreen(repository);
			return this;

		case "delete":
			// Delete account
			// TODO
			// - return new DeleteAccountScreen(repository);
			return this;

		case "quit":
			// TODO
			return this;

		default:
			throw new ScreenException("Unknown command: " + command);
		}
	}

	/**
	 * Handles a player command.
	 * @param command 		Command
	 * @param name			Player name
	 * @param session 		Session
	 * @return Next screen
	 */
	private Screen handle(String command, String name, Session session) {
		// Lookup player
		final Account.PlayerSummary player = session.account().players().stream()
			.filter(p -> p.name().equals(name))
			.findAny()
			.orElseThrow(() -> new ScreenException("Unknown player: " + name));

		// Handle command
		switch(command) {
		case "play":
			// Start playing
			final PlayerCharacter pc = repository.load(player.name());
			// TODO
			// - init(EVC)
			// - set location to last save-point
			session.set(pc);
			return play;

		case "delete":
			// TODO
			// - return new DeletePlayerScreen(repository);
			return this;

		default:
			throw new ScreenException("Unknown player command: " + name);
		}
	}
}
