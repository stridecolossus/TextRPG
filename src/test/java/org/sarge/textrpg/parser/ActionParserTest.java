package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.runner.ActionDescriptor;
import org.sarge.textrpg.runner.ActionDescriptor.ActionParameter;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class ActionParserTest extends ActionTestBase {
	private ActionParser parser;
	private ActionDescriptor action;
	private ArgumentParserGroup group;
	private NameStore store;
	private Object arg;
	private Command command;
	private List<ParserResult.Reason> reasons;

	@BeforeEach
	public void before() {
		parser = new ActionParser();
		action = mock(ActionDescriptor.class);
		group = new ArgumentParserGroup();
		store = mock(NameStore.class);
		arg = new Object();
		command = null;
		reasons = new ArrayList<>();
		final AbstractAction instance = mock(AbstractAction.class);
		when(action.action()).thenReturn(instance);
		when(instance.parsers(actor)).thenReturn(ArgumentParser.Registry.EMPTY);
	}

	/**
	 * Adds a parameter.
	 */
	private void addParameter() {
		when(action.parameters()).thenReturn(List.of(new ActionParameter(Object.class, null)));
	}

	/**
	 * Adds an argument.
	 */
	private void addArgument() {
		final ArgumentParser<?> parser = cursor -> {
			if(cursor.next().equals("arg")) {
				return arg;
			}
			else {
				return null;
			}
		};

		final ArgumentParser.Registry registry = ignore -> List.of(parser);
		group.add(() -> registry);
	}

	/**
	 * Runs the parser.
	 * @param line				Command line
	 * @param expected			Whether expected to succeed
	 */
	public void run(String line, boolean expected) {
		// Parse command line
		final WordCursor cursor = new WordCursor(line, store, Set.of());
		final ParserResult result = parser.parse(actor, action, group, cursor, reasons);
		assertEquals(expected, result.isParsed());

		// Extract command
		command = result.command();

		// Check result
		if(expected) {
			assertNotNull(command);
			assertEquals(actor, command.actor());
			assertEquals(action, command.action());
			assertEquals(true, reasons.isEmpty());
		}
	}

	@Test
	public void parse() {
		run("verb", true);
		assertEquals(AbstractAction.Effort.NORMAL, command.effort());
	}

	@Test
	public void parseUnusedWords() {
		run("verb unused", false);
	}

	@Test
	public void parseArgument() {
		addParameter();
		addArgument();
		run("verb arg", true);
		assertEquals(AbstractAction.Effort.NORMAL, command.effort());
	}

	@Test
	public void parseArgumentInsufficientWords() {
		addParameter();
		assertThrows(IllegalStateException.class, () -> run("verb", false));
	}

	@Test
	public void parseArgumentUnsupportedType() {
		addParameter();
		assertThrows(UnsupportedOperationException.class, () -> run("verb arg", false));
	}

	@Test
	public void parseArgumentIncorrectType() {
		when(action.parameters()).thenReturn(List.of(new ActionParameter(String.class, null)));
		addArgument();
		run("verb arg", false);
		assertEquals(1, reasons.size());
		assertEquals(ParserResult.Reason.MISMATCH, reasons.iterator().next());
	}

	@Test
	public void parseArgumentFailed() {
		addParameter();
		addArgument();
		run("verb cobblers", false);
	}

	@Test
	public void parseArgumentActionParser() {
		addParameter();
		final ArgumentParser<?> parser = cursor -> {
			cursor.next();
			return arg;
		};
		when(action.action().parsers(actor)).thenReturn(type -> List.of(parser));
		run("verb arg", true);
	}

	@Test
	public void parseEffortArgument() {
		when(action.isEffortAction()).thenReturn(true);
		when(store.matches("effort.fast", "fast")).thenReturn(true);
		run("verb fast", true);
		assertEquals(AbstractAction.Effort.FAST, command.effort());
	}
}
