package org.sarge.textrpg.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.util.Percentile;

public class ThingTest {
	private Thing thing;
	
	@Before
	public void before() {
		thing = new Thing() {
			@Override
			public Percentile getVisibility() {
				return null;
			}
			
			@Override
			public long getForgetPeriod() {
				return 0;
			}
			
			@Override
			public int weight() {
				return 0;
			}
			
			@Override
			public Size getSize() {
				return null;
			}
			
			@Override
			public String getName() {
				return "thing";
			}
			
			@Override
			public Description describe() {
				return null;
			}
		};
	}
	
	@Test
	public void constructor() {
		assertEquals(Thing.LIMBO, thing.getParent());
		assertEquals(true, thing.isDead());
		assertEquals(false, thing.isQuiet());
	}
	
	private static Parent createParent() {
		final Contents contents = new Contents();
		final Parent parent = mock(Parent.class);
		when(parent.getContents()).thenReturn(contents);
		return parent;
	}
	
	@Test
	public void setParent() throws ActionException {
		// Move to a parent
		final Parent parent = createParent();
		thing.setParent(parent);
		assertEquals(parent, thing.getParent());
		assertEquals(1, parent.getContents().stream().count());
		assertEquals(thing, parent.getContents().stream().iterator().next());
		
		// Move to a different parent
		final Parent other = createParent();
		thing.setParent(other);
		assertEquals(other, thing.getParent());
		assertEquals(0, parent.getContents().stream().count());
	}
	
	@Test
	public void setParentAncestor() {
		// Create parent
		final Parent parent = createParent();

		// Create actor that cannot accept
		final Contents contents = mock(Contents.class);
		final Actor actor = mock(Actor.class);
		when(actor.getContents()).thenReturn(contents);
		when(contents.getReason(thing)).thenReturn("doh");
		when(actor.getParent()).thenReturn(parent);
		
		// Set parent and check moved to root and actor is notified
		thing.setParentAncestor(actor);
		assertEquals(parent, thing.getParent());
//		verify(actor).alert(new Message("doh", thing));
	}

	@Test
	public void path() throws ActionException {
		final Parent parent = createParent();
		thing.setParent(parent);
		assertArrayEquals(new Parent[]{parent}, thing.path().toArray());
	}
	
	@Test
	public void root() throws ActionException {
		final Parent parent = createParent();
		thing.setParent(parent);
		assertEquals(parent, thing.root());
	}
	
	@Test
	public void destroy() throws ActionException {
		final Parent parent = createParent();
		thing.setParent(parent);
		thing.destroy();
		assertEquals(Thing.LIMBO, thing.getParent());
	}
}
