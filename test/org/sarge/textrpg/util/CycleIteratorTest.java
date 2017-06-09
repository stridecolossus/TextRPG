package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Test;

public class CycleIteratorTest {
	@Test
	public void iterator() {
		// Check forwards iteration
		final Iterator<String> itr = new CycleIterator<>(Arrays.asList("1", "2", "3"));
		assertEquals(true, itr.hasNext());
		assertEquals("1", itr.next());
		assertEquals("2", itr.next());
		assertEquals("3", itr.next());
		
		// Check reverse
		assertEquals(true, itr.hasNext());
		assertEquals("2", itr.next());
		assertEquals("1", itr.next());
		
		// Check re-start
		assertEquals(true, itr.hasNext());
		assertEquals("2", itr.next());
	}
	
	@Test
	public void empty() {
		final Iterator<String> itr = new CycleIterator<String>(Collections.emptyList());
		assertEquals(false, itr.hasNext());
	}
}
