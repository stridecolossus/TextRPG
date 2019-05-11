package org.sarge.textrpg.parser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.sarge.lib.collection.StrictMap;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.parser.ParserResult.Reason;
import org.sarge.textrpg.runner.ActionDescriptor;
import org.sarge.textrpg.runner.ActionDescriptor.ActionParameter;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.util.WordCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Parser for an action.
 * @author Sarge
 */
@Component
public class ActionParser {
	private static final Logger LOG = LoggerFactory.getLogger(ActionParser.class);

	private static final ParserResult MISMATCH = new ParserResult(ParserResult.Reason.MISMATCH);
	private static final ArgumentParser<AbstractAction.Effort> EFFORT_PARSER = new EnumArgumentParser<>("effort", AbstractAction.Effort.class);

	private final AtomicLong generator = new AtomicLong();
	private final transient Map<Class<?>, Object> matched = new StrictMap<>();

	/**
	 * Records a matched argument.
	 * @param type		Parameter type
	 * @param arg		Argument
	 */
	private void add(Class<?> type, Object arg) {
		assert !matched.containsKey(type);
		matched.put(type, arg);
	}

	/**
	 * Parses the command against the given action.
	 * @param actor			Actor
	 * @param action 		Action descriptor
	 * @param group			Parser group
	 * @param cursor		Command word cursor
	 * @param reasons		Cumulative failure reasons (mutable side-effect)
	 * @return Parsed command or <tt>null</tt> for a syntax error
	 */
	public ParserResult parse(PlayerCharacter actor, ActionDescriptor action, ArgumentParserGroup group, WordCursor cursor, List<Reason> reasons) {
		// Init next action
		final String id = TextHelper.wrap(String.valueOf(generator.incrementAndGet()), '[', ']');
		final Object[] args = new Object[action.parameters().size()];
		LOG.debug("{} Trying: {}", id, action);

		// Reset words and skip verb
		cursor.reset();
		cursor.next();

		// Check minimum number of words available
		final List<ActionParameter> params = action.parameters();
		final int size = params.size();
		if(!cursor.capacity(size + 1)) throw new IllegalStateException(String.format("Insufficient capacity: size=%d cursor=%s", size, cursor));

		// Init parsers for this action
		group.add(() -> action.action().parsers(actor));

		// Find matching arguments
		for(int n = 0; n < size; ++n) {
			// Init context for the next argument
			matched.clear();

			// Lookup parsers for the next parameter
			LOG.debug("{} Parsing: index={} param={}", id, n, params.get(n));
			final Class<?> type = params.get(n).type();
			final Iterator<ArgumentParser<?>> iterator = group.iterator(type);
			if(!iterator.hasNext()) throw new UnsupportedOperationException("Unsupported argument type: " + type);

			// Apply candidate parsers
			while(iterator.hasNext()) {
				// Lookup parser
				final ArgumentParser<?> parser = iterator.next();
				LOG.debug("{} Parsing: {}", id, parser);

				// Skip parsers that are not applicable for the remaining command words
				final int min = parser.count() + (size - n) - 1;
				if(!cursor.remaining(min)) {
					LOG.debug("{} Insufficient capacity: min={}", id, min);
					continue;
				}

				// Match argument
				cursor.mark();
				final Object obj = match(type, parser, cursor);
				if(obj == null) {
					cursor.back();
					continue;
				}

				// Stop if parsed but incorrect type
				if(!type.isAssignableFrom(obj.getClass())) {
					LOG.debug("{} Incorrect type: actual={} expected={}", id, obj.getClass().getName(), type);
					add(type, obj);
					reasons.add(Reason.MISMATCH);
					return MISMATCH;
				}

				// Otherwise found matching argument
				LOG.debug("{} Matched: arg={}", id, obj);
				args[n] = obj;
				break;
			}

			// Stop if argument not parsed
			if(args[n] == null) {
				LOG.debug("{} Failed", id);
				return ParserResult.FAILED;
			}
		}

		// Parse optional effort argument
		final AbstractAction.Effort effort = parseEffortArgument(action, cursor);
		if(effort == null) {
			LOG.debug("{} Effort failed", id);
			return ParserResult.FAILED;
		}

		// Check all words consumed
		if(!cursor.isExhausted()) {
			LOG.debug("{} Unused words", id);
			return ParserResult.FAILED;
		}

		// Found matching command
		LOG.debug("{} Matched successfully", id);
		return new ParserResult(new Command(actor, action, Arrays.asList(args), effort));		// TODO - array OR list FFS!
	}

	/**
	 * Matches an argument either via the given parser or a previously matched argument.
	 * @param type			Type
	 * @param parser		Parser
	 * @return Matched argument or <tt>null</tt> if none
	 */
	private Object match(Class<?> type, ArgumentParser<?> parser, WordCursor cursor) {
		final Object prev = matched.get(type);
		if(prev == null) {
			// Apply parser
			return parser.parse(cursor);
		}
		else {
			// Re-use previously matched argument
			cursor.next();
			return prev;
		}
	}

	/**
	 * Determines the optional effort argument.
	 * @param action		Action
	 * @param cursor		Cursor
	 * @return Effort argument
	 */
	private static AbstractAction.Effort parseEffortArgument(ActionDescriptor action, WordCursor cursor) {
		if(action.isEffortAction() && cursor.remaining(1)) {
			return EFFORT_PARSER.parse(cursor);
		}
		else {
			return AbstractAction.Effort.NORMAL;
		}
	}
}
