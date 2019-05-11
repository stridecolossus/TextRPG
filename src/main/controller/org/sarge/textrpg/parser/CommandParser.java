package org.sarge.textrpg.parser;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.parser.ParserResult.Reason;
import org.sarge.textrpg.runner.ActionDescriptor;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The <i>command parser</i> parses a command string into a {@link ActionParserResult}.
 * <p>
 * The parsing process consists of the following steps:
 * <ol>
 * <li>Extract the command verb</li>
 * <li>Enumerate candidate actions for the verb</li>
 * <li>Match command words against the action arguments</li>
 * <li>Match the optional effort argument</li>
 * </ol>
 * @author Sarge
 */
@Component
public class CommandParser {
	@Autowired
	private final List<ActionDescriptor> actions = new StrictList<>();

	private final ArgumentParser.Registry registry;
	private final ActionParser parser;
	private final NameStore store;
	private final Set<String> stop;

	private transient final Map<String, List<ActionDescriptor>> cache = new HashMap<>();

	/**
	 * Constructor.
	 * @param parser		Action parser
	 * @param registry		Standard parsers registry
	 * @param store 		Actions name-store
	 * @param stop			Stop words
	 */
	public CommandParser(ActionParser parser, ArgumentParser.Registry registry, NameStore store, @Value("${stop.words}") Set<String> stop) {
		this.parser = notNull(parser);
		this.registry = notNull(registry);
		this.store = notNull(store);
		this.stop = Set.copyOf(stop);
	}

	/**
	 * Registers an action.
	 * @param action Action to add
	 */
	public void add(ActionDescriptor action) {
		actions.add(action);
	}

	/**
	 * Parses the given command.
	 * @param actor			Actor
	 * @param line			Command line
	 * @param store			Name-store for the given actor
	 * @param def			Thing argument parser for the given actor
	 * @return Result
	 * @throws IllegalArgumentException if the command is empty
	 */
	public ParserResult parse(PlayerCharacter actor, String line, NameStore store, ArgumentParser<?> def) {
		// Create cursor
		final WordCursor cursor = new WordCursor(line.trim().toLowerCase(), store, stop);

		// Extract verb
		if(!cursor.remaining(1)) throw new IllegalArgumentException("Empty command line");
		final String verb = cursor.next();

		// Init parsers for the actor
		final ArgumentParser.Registry local = type -> {
			final ArgumentParser<?> prev = new PreviousObjectArgumentParser(actor);
			if(isThingType(type)) {
				return List.of(def, prev);
			}
			else {
				return List.of();
			}
		};

		// Create parser group
		final ArgumentParserGroup group = new ArgumentParserGroup();
		group.add(() -> registry);
		group.add(() -> local);

		// Enumerate matching actions
		// TODO - will get cluttered with empty lists for cobblers commands
		final List<ActionDescriptor> matched = cache.computeIfAbsent(verb, this::actions);
		if(matched.isEmpty()) return ParserResult.FAILED;

		// Parse candidate actions (and accumulate reasons as a nasty side-effect)
		final List<Reason> reasons = new ArrayList<>();
		return matched.stream()
			.filter(action -> cursor.capacity(action.parameters().size() + 1))
			.map(action -> parser.parse(actor, action, group, cursor, reasons))
			.dropWhile(StreamUtil.not(ParserResult::isParsed))
			.findAny()
			.orElseGet(() -> ParserResult.merge(reasons));
	}

	/**
	 * Finds actions matching the given verb.
	 */
	private List<ActionDescriptor> actions(String verb) {
		return actions.stream().filter(action -> this.store.matches(action.name(), verb)).collect(toList());
	}

	/**
	 * @param type Parameter type
	 * @return Whether the given type is-a <i>thing</i>
	 */
	private static boolean isThingType(Class<?> type) {
		if(type == Openable.class) return true;
		if(type == Parent.class) return true;
		if(Thing.class.isAssignableFrom(type)) return true;
		return false;
	}
}
