package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.util.WordCursor;

public class PreviousObjectArgumentParserTest {
	private PreviousObjectArgumentParser parser;
	private PlayerCharacter player;
	private WordCursor cursor;

	@BeforeEach
	public void before() {
		player = mock(PlayerCharacter.class);
		parser = new PreviousObjectArgumentParser(player);
		cursor = mock(WordCursor.class);
		final String arg = "previous.object";
		when(cursor.matches(arg)).thenReturn(true);
	}

	@Test
	public void parse() {
		final Thing prev = mock(Thing.class);
		when(player.previous()).thenReturn(prev);
		assertEquals(prev, parser.parse(cursor));
	}

	@Test
	public void parseNone() {
		assertEquals(null, parser.parse(cursor));
	}
}
