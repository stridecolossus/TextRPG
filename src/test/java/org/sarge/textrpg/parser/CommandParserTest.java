package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.runner.ActionDescriptor;
import org.sarge.textrpg.runner.ActionDescriptor.ActionParameter;
import org.sarge.textrpg.util.NameStore;

public class CommandParserTest {
	private CommandParser parser;
	private ActionParser action;
	private NameStore store;
	private PlayerCharacter actor;
	private ArgumentParser<?> def;
	private ActionDescriptor descriptor;

	@BeforeEach
	public void before() {
		actor = mock(PlayerCharacter.class);
		def = mock(ArgumentParser.class);
		action = mock(ActionParser.class);
		store = mock(NameStore.class);
		parser = new CommandParser(action, mock(ArgumentParser.Registry.class), store, Set.of());
		descriptor = mock(ActionDescriptor.class);
		when(descriptor.name()).thenReturn("verb");
		parser.add(descriptor);
	}

	@Test
	public void parseEmptyCommand() {
		assertThrows(IllegalArgumentException.class, () -> parser.parse(actor, "", store, def));
	}

	@Test
	public void parseAction() {
		when(store.matches("verb", "verb")).thenReturn(true);
		when(action.parse(eq(actor), eq(descriptor), any(), any(), any())).thenReturn(new ParserResult(mock(Command.class)));
		final ParserResult result = parser.parse(actor, "verb", store, def);
		assertEquals(true, result.isParsed());
	}

	@Test
	public void parseUnknownAction() {
		assertEquals(ParserResult.FAILED, parser.parse(actor, "cobblers", store, def));
	}

	@Test
	public void parseInsufficientWords() {
		when(descriptor.parameters()).thenReturn(List.of(new ActionParameter(Object.class, null)));
		assertEquals(ParserResult.FAILED, parser.parse(actor, "verb", store, def));
	}
}
