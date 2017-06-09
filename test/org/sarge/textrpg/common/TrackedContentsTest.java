package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.object.TrackedContents;
import org.sarge.textrpg.object.TrackedContents.Limit;
import org.sarge.textrpg.object.WorldObject;

public class TrackedContentsTest extends ActionTest {
	private TrackedContents contents;
	private WorldObject obj;
	
	@Before
	public void before() {
		contents = new TrackedContents(Collections.singletonMap(Limit.number(1), "limit"));
		obj = mock(WorldObject.class);
		when(obj.getWeight()).thenReturn(1);
		when(obj.getSize()).thenReturn(Size.MEDIUM);
	}
	
	@Test
	public void getWeight() throws ActionException {
		// Check initially empty
		assertEquals(0, contents.getWeight());

		// Add weight
		contents.add(obj);
		assertEquals(1, contents.getWeight());

		// Remove weight
		contents.remove(obj);
		assertEquals(0, contents.getWeight());
	}
	
	@Test
	public void getReason() throws ActionException {
		assertEquals(null, contents.getReason(obj));
		contents.add(obj);
		assertEquals("contents.add.limit", contents.getReason(obj));
	}

	@Test
	public void numberLimit() throws ActionException {
		final Limit number = Limit.number(1);
		assertEquals(false, number.exceeds(obj, contents));
		contents.add(obj);
		assertEquals(true, number.exceeds(obj, contents));
	}
	
	@Test
	public void weightLimit() {
		final Limit weight = Limit.weight(1);
		assertEquals(false, weight.exceeds(obj, contents));
		when(obj.getWeight()).thenReturn(2);
		assertEquals(true, weight.exceeds(obj, contents));
	}
	
	@Test
	public void sizeLimit() {
		final Limit size = Limit.size(Size.MEDIUM);
		assertEquals(false, size.exceeds(obj, contents));
		when(obj.getSize()).thenReturn(Size.LARGE);
		assertEquals(true, size.exceeds(obj, contents));
	}
}
