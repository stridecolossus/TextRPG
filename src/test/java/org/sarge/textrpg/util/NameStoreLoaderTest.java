package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NameStoreLoaderTest {
	private NameStoreLoader loader;

	@BeforeEach
	public void before() {
		loader = new NameStoreLoader();
	}

	@Test
	public void loadInvalidEntry() throws IOException {
		assertThrows(IOException.class, () -> loader.load(new StringReader("key name")));
	}

	@Test
	public void loadEmptyKey() throws IOException {
		assertThrows(IOException.class, () -> loader.load(new StringReader(": name")));
	}

	@Test
	public void loadEmptyName() throws IOException {
		assertThrows(IOException.class, () -> loader.load(new StringReader("key:")));
	}

	@Test
	public void loadComment() throws IOException {
		loader.load(new StringReader("# comment"));
		assertEquals(true, loader.build().isEmpty());
	}

	@Test
	public void loadEmpty() throws IOException {
		loader.load(new StringReader(""));
		assertEquals(true, loader.build().isEmpty());
	}

	@Test
	public void loadSingleName() throws IOException {
		loader.load(new StringReader("key: name"));
		final NameStore store = loader.build();
		assertNotNull(store);
		assertTrue(store instanceof DefaultNameStore);
		assertEquals("name", store.get("key"));
	}

	@Test
	public void loadArray() throws IOException {
		loader.load(new StringReader("key: one | two"));
		final NameStore store = loader.build();
		assertNotNull(store);
		assertTrue(store instanceof ArrayNameStore);
		assertEquals("one", store.get("key"));
		assertEquals(true, store.matches("key", "one"));
		assertEquals(true, store.matches("key", "two"));
	}

	@Test
	public void loadArrayEmptyEntry() throws IOException {
		assertThrows(IOException.class, () -> loader.load(new StringReader("key: one | | two")));
	}

	@Test
	public void loadIndented() throws IOException {
		loader.load(new StringReader("key: name \n\t sub: indented"));
		final NameStore store = loader.build();
		assertNotNull(store);
		assertTrue(store instanceof DefaultNameStore);
		assertEquals("name", store.get("key"));
		assertEquals("indented", store.get("key.sub"));
	}

	@Test
	public void loadIndentedNoPreviousEntry() throws IOException {
		assertThrows(IOException.class, () -> loader.load(new StringReader("\tkey: name")));
	}
}
