package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.ResponseFormatter;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.EntityValueController;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.parser.Command;
import org.sarge.textrpg.parser.CommandParser;
import org.sarge.textrpg.parser.ParserResult;
import org.sarge.textrpg.parser.ThingArgumentParser;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.LightLevelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Active play session screen.
 * @author Sarge
 */
@Component("screen.play")
public class PlayScreen implements Screen {
	private static final Logger LOG = LoggerFactory.getLogger(PlayScreen.class);

	/**
	 * Convenience wrapper for command processing.
	 */
	@Component
	static class CommandProcessor {
		@Autowired private CommandParser parser;
		@Autowired private CommandExecutor executor;
		@Autowired private LightLevelProvider light;

		/**
		 * Processes a command.
		 * @param line			Command string
		 * @param actor			Actor
		 * @return Response
		 * @throws ActionException if the command cannot be processed
		 */
		public Response process(String line, PlayerCharacter actor, NameStore store) throws ActionException {
			// Parse command
			final ArgumentParser<?> def = new ThingArgumentParser(actor, light); // TODO - cache this in session/player?
			final ParserResult result = parser.parse(actor, line, store, def);

			// Display failed result
			if(!result.isParsed()) {
				return Response.of(TextHelper.join("parser.command", result.reason()));
			}

			// Update previous object
			final Command command = result.command();
			if(command.arguments().size() > 0) {
				final Object arg = command.arguments().get(0);
				if(Thing.class.isAssignableFrom(arg.getClass())) {
					actor.setPrevious((Thing) arg);
				}
			}
			else {
				actor.setPrevious(null);
			}

			// Execute command
			final Response response = executor.execute(command, light);

			// Start inductions
			response.induction().ifPresent(induction -> actor.manager().induction().start(induction));

			return response;
		}
	}

	private final CommandProcessor proc;
	private final EntityValueController update;
	private final ResponseFormatter formatter;
	private final NameStore store;

	private Screen killed;

	/**
	 * Constructor.
	 * @param proc				Command processor
	 * @param update			Updater
	 * @param formatter			Response formatter
	 * @param store				Default name-store
	 */
	public PlayScreen(CommandProcessor proc, EntityValueController update, ResponseFormatter formatter, NameStore store) {
		this.proc = notNull(proc);
		this.update = notNull(update);
		this.formatter = notNull(formatter);
		this.store = notNull(store);
	}

	/**
	 * Sets the screen for a killed player.
	 * @param killed Killed screen
	 */
	@Autowired
	public void setKilledScreen(Screen killed) {
		this.killed = notNull(killed);
	}

	@Override
	public void init(Session session) {
		// Init name-store
		final PlayerCharacter player = session.player();
		final Area area = player.location().area();
		final NameStore store = session.init(area, this.store);

		// Display location
		final String description = formatter.format(player, store, Response.DISPLAY_LOCATION);
		session.write(description);
	}

	@Override
	public Screen handle(Session session, String command) throws ScreenException {
		// Handle quit command
		if("quit".equals(command)) {
			// TODO
			System.exit(0);
			//return new QuitScreen();
		}

		// Update player values
		final PlayerCharacter player = session.player();
		final Area prev = player.location().area();
		update.update(player);

		// Process command
		final NameStore store = session.store();
		try {
			final Response response = proc.process(command, player, store);
			final String result = formatter.format(player, store, response);
			session.write(result);
		}
		catch(ActionException e) {
			final String message = formatter.formatter().format(e.description(), store);
			session.write(message);
		}
		catch(Exception e) {
			LOG.error("Unhandled exception during command processing", e);
			LOG.error(String.format("[%s]", command));
		}

		// Check whether killed
		if(!player.isAlive()) {
			return killed;
		}

		// Update name-store on area transitions
		final Area area = player.location().area();
		if(prev != area) {
			session.init(area, this.store);
		}

		return this;
	}

	// TODO - temporary
	public void display(Session session, Response response) {
		final String result = formatter.format(session.player(), session.store(), response);
		session.write(result);
	}
}
