package org.sarge.textrpg.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Contents.Listener;

public class ContentsTest {
	private Contents contents;
	private Thing obj;
	
	@Before
	public void before() {
		contents = new Contents();
		obj = mock(Thing.class);
	}
	
	@Test
	public void add() throws ActionException {
		contents.add(obj);
		assertEquals(1, contents.size());
		assertEquals(obj, contents.stream().iterator().next());
	}
	
	@Test
	public void remove() throws ActionException {
		contents.add(obj);
		contents.remove(obj);
		assertEquals(0, contents.size());
	}
	
	@Test
	public void listener() {
		final Listener listener = mock(Listener.class);
		contents.add(listener);
		contents.add(obj);
		contents.remove(obj);
		verify(listener).contentsChanged(true, obj);
		verify(listener).contentsChanged(false, obj);
	}
	
	private static Thing create(Thing t) {
		final Thing thing = mock(Thing.class, withSettings().extraInterfaces(Parent.class));
		final Contents c = new Contents();
		final Parent p = (Parent) thing;
		when(p.getContents()).thenReturn(c);
		if(t != null) {
			c.add(t);
		}
		return thing;
	}
	
	@Test
	public void recurse() {
		// Build a three-level set of contents
		final Thing bottom = create(null);
		final Thing middle = create(bottom);
		final Thing top = create(middle);
		contents.add(top);
		
		// Check recursion depth limit
		assertArrayEquals(new Thing[]{top, middle, bottom}, contents.stream(2).toArray());
		assertArrayEquals(new Thing[]{top, middle}, contents.stream(1).toArray());
	}
	
	@Test
	public void move() {
		final Contents other = new Contents();
		final Parent parent = mock(Parent.class);
		when(parent.getContents()).thenReturn(other);
		contents.add(obj);
		contents.move(parent);
		verify(obj).setParentAncestor(parent);
	}

	@Test(expected = RuntimeException.class)
	public void immutable() throws ActionException {
		Contents.EMPTY.add(obj);
	}
}
