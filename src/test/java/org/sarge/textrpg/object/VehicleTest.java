package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.object.Vehicle.Descriptor;
import org.sarge.textrpg.world.Route;

public class VehicleTest extends ActionTest {
	private Vehicle vehicle;

	@Before
	public void before() {
		final Descriptor descriptor = new Descriptor(new ContentsObjectDescriptor(new ObjectDescriptor("cart"), Collections.emptyMap()), Collections.singleton(Route.LANE), 1);
		vehicle = descriptor.create();
	}

	@Test
	public void constructor() {
		assertEquals("vehicle", vehicle.getParentName());
		assertNotNull(vehicle.getContents());
	}

	@Test
	public void verify() throws ActionException {
		assertEquals(true, vehicle.getDescriptor().isValid(Route.LANE));
		assertEquals(false, vehicle.getDescriptor().isValid(Route.PATH));
	}
}
