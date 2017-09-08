package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Route;

public class ContainerLinkTest extends ActionTest {
	private ContainerLink link;
	private ObjectDescriptor descriptor;

	@Before
	public void before() {
		descriptor = new ObjectDescriptor("ladder");
		link = new ContainerLink(Route.NONE, Script.NONE, Size.NONE, "name", descriptor);
	}

	@Test
	public void constructor() {
		assertEquals(Route.NONE, link.route());
		assertEquals(Script.NONE, link.script());
		assertNotNull(link.controller());
		assertEquals(true, link.controller().isPresent());
		assertEquals("name", link.controller().get().toString());
		assertEquals(Percentile.ONE, link.controller().get().visibility());
		assertEquals(true, link.controller().get().isQuiet());
		assertEquals(0L, link.controller().get().forgetPeriod());
		assertEquals(false, link.isTraversable(null));
	}

	@Test
	public void put() throws ActionException {
		final WorldObject obj = descriptor.create();
		link.put(obj);
		assertEquals(true, link.isTraversable(null));
		assertNotNull(link.controller());
		assertEquals(true, link.controller().isPresent());
		assertEquals(Optional.of(obj), link.controller());
	}

	@Test
	public void putRemoved() throws ActionException {
		final WorldObject obj = descriptor.create();
		link.put(obj);
		obj.setParent(loc);
		assertEquals(false, link.isTraversable(null));
	}

	@Test
	public void putOccupied() throws ActionException {
		final WorldObject obj = descriptor.create();
		link.put(obj);
		expect("object.link.occupied");
		link.put(obj);
	}

	@Test
	public void putInvalidObject() throws ActionException {
		final WorldObject obj = new ObjectDescriptor("invalid").create();
		expect("object.link.invalid");
		link.put(obj);
	}
}
