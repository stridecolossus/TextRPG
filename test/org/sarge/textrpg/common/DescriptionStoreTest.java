package org.sarge.textrpg.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.DescriptionStore.Repository;

public class DescriptionStoreTest {
	private DescriptionStore.Repository repo;
	
	@Before
	public void before() {
		repo = new Repository();
	}
	
	@Test
	public void constructor() {
		final DescriptionStore root = repo.find(Locale.ROOT);
		assertNotNull(root);
		assertEquals(null, root.getParent());
	}
	
	@Test
	public void getParent() {
		final DescriptionStore root = repo.find(Locale.ROOT);
		final DescriptionStore store = repo.find(Locale.UK);
		assertEquals(root, store.getParent());
	}

	@Test
	public void add() {
		final DescriptionStore store = repo.find(Locale.UK);
		store.add("key", "value | other");
		assertEquals("value", store.getString("key"));
		assertArrayEquals(new String[]{"value", "other"}, store.getStringArray("key"));
	}

	@Test
	public void addReference() {
		final DescriptionStore store = repo.find(Locale.UK);
		store.add("one", "value");
		store.add("two", "@one");
		assertEquals("value", store.getString("two"));
	}

	@Test
	public void hierarchy() {
		// Create parent store
		final DescriptionStore eng = repo.find(Locale.ENGLISH);
		eng.add("common", "eng");
		eng.add("key", "value");

		// Create child store
		final DescriptionStore uk = repo.find(Locale.UK);
		uk.add("common", "uk");
		
		// Check getters
		assertEquals("uk", uk.getString("common"));
		assertEquals("value", uk.getString("key"));
	}
}
