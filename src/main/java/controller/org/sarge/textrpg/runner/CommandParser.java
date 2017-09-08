package org.sarge.textrpg.runner;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.DescriptionStore;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.object.ObjectFilter;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Location;

/**
 * Parser for commands.
 * @author Sarge
 */
public class CommandParser {
    /**
     * Action definition.
     */
    private class ActionEntry {
        private final AbstractAction action;
        private final List<Method> methods;

        /**
         * Constructor.
         * @param action Action-class
         */
        private ActionEntry(AbstractAction action) {
            this.action = action;
            this.methods = enumerateMethods(action);
        }

        private List<Method> enumerateMethods(AbstractAction action){
            return Arrays.stream(action.getClass().getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .collect(toList());
        }

        public Method findMethod(Class<?>[] params) {
            for(Method m : methods) {
                if(matches(m, params)) return m;
            }
            return null;
        }
    }


	private final List<ActionEntry> actions;
	private final Set<String> stopwords;
	private final DescriptionStore store;

	/**
	 * Constructor.
	 * @param actions		Actions
	 * @param stopwords		Stop-words
	 * @param actions		Actions indexed by name
	 */
	public CommandParser(List<AbstractAction> actions, Set<String> stopwords, DescriptionStore store) {
		this.actions = actions.stream().map(ActionEntry::new).collect(toList());
		this.stopwords = new HashSet<>(stopwords);
		this.store = Check.notNull(store);
	}

	/**
	 * Parses the given command string.
	 * @param player	Player
	 * @param cmd		Command string
	 * @return Command
	 * @throws ActionException if the command is not valid
	 */
	public Command parse(Player player, String cmd) throws ActionException {
		// Tokenize command string
		final String[] tokens = cmd.trim().toLowerCase().split(" ");
		final List<String> words = new ArrayList<>(Arrays.asList(tokens));

		// Strip stop-words
		words.removeIf(String::isEmpty);
		words.removeIf(stopwords::contains);
		if(words.isEmpty()) throw new ActionException("parser.empty.command");

		// Build argument sources
		final Location loc = player.location();
		final ArgumentBuilder[] builders = {
			ArgumentBuilder.enumeration(Direction.class),
			ArgumentBuilder.object(ObjectFilter.ALL),
			ArgumentBuilder.object(WorldObject.PREVIOUS),
			ArgumentBuilder.of(player.contents()),
			ArgumentBuilder.of(loc.contents()),
			new PortalsArgumentBuilder(loc),
			new TopicsArgumentBuilder(loc.contents()),
			arg -> ObjectFilter.FILTERS.stream(),
			arg -> loc.getDecorations(),
			arg -> Location.getSurfaces(),
		};

		// Find arguments
		final Object[] args = new Object[words.size() - 1];
		for(int n = 0; n < args.length; ++n) {
			args[n] = find(player, builders, words.get(n + 1));
		}

		// Find action
		return find(player, words.get(0), args);
	}

	/**
	 * Finds an argument in the actors inventory or the current location.
	 * @param actor			Actor
	 * @param builders		Argument builders
	 * @param name			Name
	 * @return Object
	 * @throws ActionException if the specified argument cannot be found
	 */
	private Object find(Entity actor, ArgumentBuilder[] builders, String name) throws ActionException {
		// Test whether argument is a number
		final int num;
		try {
			num = Integer.parseInt(name);
			return num;
		}
		catch(NumberFormatException e) {
			// Ignore
		}

		// Otherwise find argument
		return Arrays.stream(builders)
			.flatMap(b -> b.stream(actor))
			.filter(obj -> matches(obj.toString().toLowerCase(), name, true))
			.findFirst()
			.orElseThrow(() -> new ActionException("action.unknown.argument", name));
	}

	/**
	 * @param key		Names key
	 * @param word		Command word
	 * @param expand	Whether to expand uniquely named objects
	 * @return Whether the names of the given key match the command word
	 */
	private boolean matches(String key, String word, boolean expand) {
		// Lookup names for this object
		final String[] names = store.getStringArray(key);
		if(names == null) return false;

		// Find matching name
		for(String name : names) {
			if(name.equalsIgnoreCase(word)) return true;
		}

		// Find unique name
		if(expand && (names.length == 1)) {
			final List<String> tokens = new ArrayList<>(Arrays.asList(names[0].toLowerCase().split(" ")));
			tokens.removeIf(stopwords::contains);
			if(tokens.contains(word)) return true;
		}

		// Not matched
		return false;
	}

	/**
	 * Finds a action matching the given arguments.
	 * @param actor		Actor
	 * @param name		Action name
	 * @param args		Arguments
	 * @return Command
	 * @throws ActionException if no matching action can be found
	 */
	private Command find(Entity actor, String name, Object[] args) throws ActionException {
		// Build parameter-list
		final Class<?>[] params = new Class<?>[args.length];
		for(int n = 0; n < args.length; ++n) {
			params[n] = args[n].getClass();
		}

		// Find matching action
		// TODO - to stream?
		for(ActionEntry entry : actions) {
			// Match action name
			if(!matches(entry.action.getName(), name, false)) {
			    continue;
			}

			// Find method matching the arguments
			final Method method = entry.findMethod(params);
			if(method == null) {
			    continue;
			}

			// Found match
			return new Command(entry.action, method, args);
		}

		// No action found
		throw new ActionException("parser.unknown.action");
	}

	/**
	 * Determines whether an action method matches the given arguments.
	 */
	private static boolean matches(Method m, Class<?>[] args) {
		// Check matching number of arguments (excluding common arguments)
		final Class<?>[] params = m.getParameterTypes();
		if(params.length != args.length + 1) return false;

		// Check matching argument types
		for(int n = 0; n < args.length; ++n) {
			if(!params[n + 1].isAssignableFrom(args[n])) return false;
		}

		// Found matching method
		return true;
	}
}
