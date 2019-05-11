package org.sarge.textrpg.runner;

import org.springframework.stereotype.Component;

/**
 * Quit connection screen.
 * @author Sarge
 */
@Component
public class QuitScreen implements Screen {
	private final SessionManager manager;

	/**
	 * Constructor.
	 * @param manager
	 */
	public QuitScreen(SessionManager manager) {
		this.manager = manager;
	}

	@Override
	public void init(Session session) {
		// Save player
		// TODO

		// Close session
		manager.close(session);
	}

	@Override
	public Screen handle(Session session, String command) throws ScreenException {
		return this;
	}
}
