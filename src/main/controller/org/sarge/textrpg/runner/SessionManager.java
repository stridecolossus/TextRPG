package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.runner.Connection.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The <i>session manager</i> maintains the currently active sessions.
 * @see Session
 * @author Sarge
 */
@Component
public class SessionManager implements ConnectionServer.Handler {
	private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

	private final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private Screen start;

	/**
	 * Constructor.
	 * @param start Starting screen for new sessions
	 */
	public SessionManager(Screen start) {
		this.start = notNull(start);
	}

	/**
	 * @return Number of active sessions
	 */
	public int size() {
		return sessions.size();
	}

	/**
	 * @return Active players
	 */
	public Stream<PlayerCharacter> players() {
		return sessions.stream().map(Session::player).filter(Objects::nonNull);
	}

	@Override
	public Listener handle(Connection con) {
		final Session session = new Session(con, start);
		return add(session);
	}

	/**
	 * Adds a new session.
	 * @param session Session
	 * @return Client listener
	 */
	protected Listener add(Session session) {
		// Create new session
		LOG.info("New session: " + session);
		sessions.add(session);

		// Create client listener
		return new Listener() {
			@Override
			public void handle(String command) {
				session.handle(command);
			}

			@Override
			public void closed() {
				LOG.info("Session closed by client: " + session);
				close(session);
			}
		};
	}

	/**
	 * Closes the given session.
	 * @param session Session to remove
	 */
	protected void close(Session session) {
		LOG.info("Closing session: " + session);
		sessions.remove(session);
		session.close();
	}

	/**
	 * Closes <b>all</b> sessions.
	 */
	public void close() {
		LOG.info("Closing all sessions");
		final var copy = new HashSet<>(sessions);
		copy.forEach(Session::close);
		sessions.clear();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("sessions", size()).toString();
	}
}
