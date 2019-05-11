package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.parser.ParserResult.Reason;

public class ParserResultTest {
	private ParserResult success;

	@BeforeEach
	public void before() {
		final Command command = mock(Command.class);
		success = new ParserResult(command);
	}

	@Test
	public void success() {
		assertEquals(true, success.isParsed());
		assertEquals(Reason.SUCCESS, success.reason());
		assertNotNull(success.command());
	}

	@Test
	public void failed() {
		assertEquals(false, ParserResult.FAILED.isParsed());
		assertEquals(Reason.SYNTAX, ParserResult.FAILED.reason());
		assertEquals(null, ParserResult.FAILED.command());
	}

	@Test
	public void mismatch() {
		final ParserResult result = new ParserResult(Reason.MISMATCH);
		assertEquals(false, result.isParsed());
		assertEquals(Reason.MISMATCH, result.reason());
		assertEquals(null, result.command());
	}

	@Test
	public void merge() {
		final ParserResult failed = new ParserResult(Reason.SYNTAX);
		final ParserResult mismatch = new ParserResult(Reason.MISMATCH);
		assertEquals(failed, ParserResult.merge(List.of()));
		assertEquals(failed, ParserResult.merge(List.of(Reason.SYNTAX)));
		assertEquals(mismatch, ParserResult.merge(List.of(Reason.MISMATCH)));
		assertEquals(mismatch, ParserResult.merge(List.of(Reason.MISMATCH, Reason.MISMATCH)));
		assertEquals(mismatch, ParserResult.merge(List.of(Reason.SYNTAX, Reason.MISMATCH)));
		assertEquals(mismatch, ParserResult.merge(List.of(Reason.MISMATCH, Reason.SYNTAX)));
	}

	@Test
	public void mergeInvalid() {
		assertThrows(IllegalStateException.class, () -> ParserResult.merge(List.of(Reason.SUCCESS)));
	}
}
