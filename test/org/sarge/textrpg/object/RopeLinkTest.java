package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.object.Rope.Anchor;
import org.sarge.textrpg.world.Route;

public class RopeLinkTest extends ActionTest {
	private RopeLink link;
	private Anchor anchor;
	private Rope rope;
	
	@Before
	public void before() throws ActionException {
		anchor = new Rope.Anchor(new ObjectDescriptor("anchor"));
		link = new RopeLink(anchor, true);
		rope = new Rope(new Rope.Descriptor(new ObjectDescriptor("rope"), 1, 2, true));
		rope.setParent(actor);
		when(actor.perceives(anchor)).thenReturn(true);
	}
	
	@Test
	public void constructor() {
		assertEquals(Route.ROPE, link.getRoute());
		assertEquals(Optional.of(anchor), link.getController());
		assertEquals(false, link.isVisible(actor));
		assertEquals(false, link.isTraversable(actor));
	}
	
	@Test
	public void attach() throws ActionException {
		rope.attach(actor, anchor);
		assertEquals(true, link.isVisible(actor));
		assertEquals(true, link.isTraversable(actor));
		assertEquals(Optional.of(anchor), link.getController());
		rope.remove(actor);
		assertEquals(false, link.isTraversable(actor));
	}
	
	@Test
	public void describe() throws ActionException {
		assertEquals("!dir!", link.describe("dir"));
		rope.attach(actor, anchor);
		assertEquals("|dir|", link.describe("dir"));
	}
}
