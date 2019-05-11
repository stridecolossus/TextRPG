package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.LimitedContents.Limit;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.util.ActionException;

public class LimitedContentsTest {
	private Contents contents;
	private Limit limit;
	private Thing thing;

	@BeforeEach
	public void before() {
		// Create an object to be added
		thing = mock(Thing.class);
		when(thing.size()).thenReturn(Size.NONE);

		// Define some limits
		limit = mock(Limit.class);
		final var limits = Map.of(
			"limit",		limit,
			"capacity",		Limit.capacity(1),
			"size",			Limit.size(Size.MEDIUM),
			"weight",		Limit.weight(2)
		);

		// Create limited contents
		contents = new LimitedContents(new LimitsMap(limits));
		when(limit.accepts(contents, thing)).thenReturn(true);
	}

	@Test
	public void constructor() {
		assertEquals(0, contents.size());
	}

	@Test
	public void accepts() throws ActionException {
		assertEquals(Optional.empty(), contents.reason(thing));
		contents.add(thing);
	}

	@Test
	public void acceptsViolation() throws ActionException {
		when(limit.accepts(contents, thing)).thenReturn(false);
		assertEquals(Optional.of("limit"), contents.reason(thing));
	}

	@Test
	public void limitCapacity() throws ActionException {
		contents.add(thing);
		assertEquals(Optional.of("capacity"), contents.reason(thing));
	}

	@Test
	public void limitSize() throws ActionException {
		when(thing.size()).thenReturn(Size.LARGE);
		assertEquals(Optional.of("size"), contents.reason(thing));
	}

	@Test
	public void limitWeight() throws ActionException {
		when(thing.weight()).thenReturn(3);
		assertEquals(Optional.of("weight"), contents.reason(thing));
	}

	@Test
	public void limitsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new LimitsMap(Map.of()));
	}

	@Test
	public void limitsDuplicate() {
		assertThrows(IllegalArgumentException.class, () -> new LimitsMap(Map.of("limit", limit, "limit", limit)));
	}
}
