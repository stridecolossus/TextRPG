package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.runner.Screen.ScreenException;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.world.Area;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <i>session</i> maintains data used during a client session.
 * @author Sarge
 */
public class Session extends AbstractObject {
	private static final Logger LOG = LoggerFactory.getLogger(Session.class);

	private final Connection connection;

	private Screen screen;
	private Account account;
	private PlayerCharacter player;
	private NameStore store;

	/**
	 * Constructor.
	 * @param connection 	Connection
	 * @param start			Starting screen
	 */
	public Session(Connection connection, Screen start) {
		this.connection = notNull(connection);
		this.screen = notNull(start);
	}

	/**
	 * Handles a client command.
	 * @param command Client command
	 */
	public void handle(String command) {
		try {
			// Delegate to screen
			final Screen next = screen.handle(Session.this, command);

			// Register new screens
			if(next != screen) {
				if(next == null) {
					LOG.error("Screen returned NULL: current={}", screen);
				}
				else {
					next.init(this);
					screen = next;
				}
			}
		}
		catch(ScreenException e) {
			connection.write(e.getMessage());
		}
		catch(Exception e) {
			LOG.error("Uncaught exception in screen handler: " + this, e);
		}
	}

	/**
	 * Writes a message to the client.
	 * @param message Message
	 */
	public void write(String message) {
		connection.write(message);
	}

	/**
	 * Closes the underlying connection.
	 */
	protected void close() {
		connection.close();
	}

	/**
	 * @return Account
	 */
	public Account account() {
		return account;
	}

	/**
	 * Sets the account associated with this session.
	 * @param account Account
	 */
	public void set(Account account) {
		this.account = account;
	}

	/**
	 * @return Player character or <tt>null</tt> if not active
	 */
	public PlayerCharacter player() {
		return player;
	}

	/**
	 * Sets the active player for this session.
	 * @param player Player
	 */
	public void set(PlayerCharacter player) {
		this.player = player;
	}

	/**
	 * @return Name-store for this session
	 */
	public NameStore store() {
		return store;
	}

	/**
	 * Initialises the current name-store.
	 * @param area		Area
	 * @param store		Global name-store
	 * @return Name-store
	 */
	public NameStore init(Area area, NameStore store) {
		this.store = NameStore.of(store, area.store());
		return this.store;
	}
}
