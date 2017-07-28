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
		assertEquals(Route.NONE, link.getRoute());
		assertEquals(Script.NONE, link.getScript());
		assertNotNull(link.getController());
		assertEquals(true, link.getController().isPresent());
		assertEquals("name", link.getController().get().toString());
		assertEquals(Percentile.ONE, link.getController().get().getVisibility());
		assertEquals(true, link.getController().get().isQuiet());
		assertEquals(0L, link.getController().get().getForgetPeriod());
		assertEquals(false, link.isTraversable(null));
	}
	
	@Test
	public void put() throws ActionException {
		final WorldObject obj = descriptor.create();
		link.put(obj);
		assertEquals(true, link.isTraversable(null));
		assertNotNull(link.getController());
		assertEquals(true, link.getController().isPresent());
		assertEquals(Optional.of(obj), link.getController());
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
