package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WordCursorTest {
	private WordCursor cursor;
	private NameStore store;

	@BeforeEach
	public void before() {
		final Set<String> stop = Set.of("the", "into");
		store = mock(NameStore.class);
		cursor = new WordCursor("put the object into the container", store, stop);
	}

	@Test
	public void constructor() {
		assertEquals(false, cursor.isExhausted());
		assertEquals(store, cursor.store());
	}

	@Test
	public void capacity() {
		assertEquals(true, cursor.remaining(0));
		assertEquals(true, cursor.remaining(1));
		assertEquals(true, cursor.remaining(2));
		assertEquals(true, cursor.remaining(3));
		assertEquals(false, cursor.remaining(4));
	}

	@Test
	public void next() {
		assertEquals("put", cursor.next());
		assertEquals("object", cursor.next());
		assertEquals("container", cursor.next());
		assertEquals(true, cursor.isExhausted());
		assertEquals(false, cursor.remaining(1));
	}

	@Test
	public void nextInsufficientCapacity() {
		for(int n = 0; n < 3; ++n) {
			cursor.next();
		}
		assertThrows(NoSuchElementException.class, () -> cursor.next());
	}

	@Test
	public void matches() {
		when(store.matches("name", "put")).thenReturn(true);
		assertEquals(true, cursor.matches("name"));
	}

	@Test
	public void matchesNotFound() {
		assertEquals(false, cursor.matches("name"));
	}

	@Test
	public void matchesInsufficientCapacity() {
		for(int n = 0; n < 3; ++n) {
			cursor.next();
		}
		assertThrows(NoSuchElementException.class, () -> cursor.matches("whatever"));
	}

	@Test
	public void mark() {
		cursor.next();
		cursor.mark();
		cursor.next();
		cursor.next();
		cursor.back();
		assertEquals("object", cursor.next());
	}

	@Test
	public void backNotMarked() {
		assertThrows(IllegalStateException.class, () -> cursor.back());
	}

	@Test
	public void backMarkConsumed() {
		cursor.mark();
		cursor.back();
		assertThrows(IllegalStateException.class, () -> cursor.back());
	}

	@Test
	public void reset() {
		cursor.next();
		cursor.reset();
		assertEquals("put", cursor.next());
	}
}
