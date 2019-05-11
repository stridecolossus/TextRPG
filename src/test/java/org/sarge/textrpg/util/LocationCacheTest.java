package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.DefaultLocation;

public class LocationCacheTest {
	private LocationCache<String> cache;
	private Function<Location, Optional<String>> mapper;
	private DefaultLocation loc;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		mapper = mock(Function.class);
		cache = new LocationCache<>(mapper);
		loc = mock(DefaultLocation.class);
	}

	@Test
	public void find() {
		final var result = Optional.of("name");
		when(mapper.apply(loc)).thenReturn(result);
		assertEquals(result, cache.find(loc));
		verify(mapper).apply(loc);
	}

	@Test
	public void findNotPresent() {
		when(mapper.apply(loc)).thenReturn(Optional.empty());
		assertEquals(Optional.empty(), cache.find(loc));
	}

	@Test
	public void findCached() {
		// Find
		final var result = Optional.of("name");
		when(mapper.apply(loc)).thenReturn(result);
		assertEquals(result, cache.find(loc));

		// find again and check cached
		assertEquals(result, cache.find(loc));
		verifyNoMoreInteractions(loc);
	}
}
