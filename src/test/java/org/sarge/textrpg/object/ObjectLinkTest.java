package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.world.Route;

public class ObjectLinkTest extends ActionTest {
	private ObjectLink link;
	private WorldObject obj;

	@Before
	public void before() {
		final Openable model = new Openable(Openable.FIXED);
		obj = mock(WorldObject.class);
		when(obj.name()).thenReturn("object");
		when(actor.perceives(obj)).thenReturn(true);
		when(obj.openableModel()).thenReturn(Optional.of(model));
		link = new ObjectLink(Route.NONE, Script.NONE, Size.NONE, obj, "reason");
	}

	@Test
	public void constructor() {
		assertEquals(false, link.isOpen());
		assertEquals(false, link.isTraversable(actor));
		assertEquals(true, link.isVisible(actor));
		assertEquals(Optional.of(obj), link.controller());
		assertEquals("reason", link.reason(actor));
		assertEquals("{object}", link.describe().build().get("object"));
	}

	@Test
	public void open() {
		obj.openableModel().get().setOpen(true);
		assertEquals(true, link.isOpen());
		assertEquals(true, link.isTraversable(actor));
		assertEquals(null, link.reason(actor));
		assertEquals(null, link.describe().build().get("object"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidObject() {
		when(obj.openableModel()).thenReturn(Optional.empty());
		new ObjectLink(Route.NONE, Script.NONE, Size.NONE, obj, "reason");
	}
}
