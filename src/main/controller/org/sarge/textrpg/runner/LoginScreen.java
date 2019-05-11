package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Account log-in screen.
 * @author Sarge
 */
@Component
@Primary
public class LoginScreen implements Screen {
	private final String welcome;
	private final AccountRepository repository;
	private Screen next;

	/**
	 * Constructor.
	 * @param welcome			Welcome banner text
	 * @param repository		Account repository
	 */
	public LoginScreen(@Value("${welcome.banner}") String welcome, AccountRepository repository) {
		this.welcome = notEmpty(welcome);
		this.repository = notNull(repository);
	}

	@Autowired
	public void setNext(@Qualifier("screen.account") Screen next) {
		this.next = next;
	}

	/**
	 * Displays the prompt.
	 */
	private static void prompt(Session session) {
		session.write("Account name: ");
	}

	@Override
	public void init(Session session) {
		session.write(welcome);
		prompt(session);
	}

	@Override
	public Screen handle(Session session, String line) {
		// Check for new account
		if("new".equals(line)) {
			// TODO
			// - return new CreateAccountScreen(repository);
			return this;
		}

		if(session.account() == null) {
			// Store account name and prompt for password
			final Account account = new Account(line);
			session.set(account);
			session.write("Password: ");
			return this;
		}
		else {
			final String name = session.account().name();
			final Optional<Account> account = repository.find(name);
			if(account.isPresent()) {
				// Log into account
				// TODO - verify credentials
				session.set(account.get());
				return next;
			}
			else {
				// Unknown account or invalid credentials
				session.set((Account) null);
				session.write("Unknown account credentials");
				prompt(session);
				return this;
			}
		}
	}
}
