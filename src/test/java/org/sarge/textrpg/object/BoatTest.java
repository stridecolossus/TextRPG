package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.object.Boat.Descriptor;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Location.Property;
import org.sarge.textrpg.world.Terrain;

public class BoatTest extends ActionTest {
	private Boat boat;
	private Location water;

	@Before
	public void before() {
		final Descriptor descriptor = new Descriptor(new ContentsObjectDescriptor(new ObjectDescriptor("cart"), Collections.emptyMap()), 1, false);
		boat = descriptor.create();
		water = new Location("water", Area.ROOT, Terrain.WATER, Collections.singleton(Property.WATER), Collections.emptyList());
	}

	@Test
	public void constructor() {
		assertEquals(true, boat.isMoored());
		assertEquals("boat.moored", boat.getFullDescriptionKey());
		assertEquals("vehicle", boat.parentName());
	}

	@Test
	public void setParentIntoWater() throws ActionException {
		boat.setParent(water);
		assertEquals(water, boat.parent());
		assertEquals(false, boat.isMoored());
	}

	@Test
	public void setParentMoored() throws ActionException {
		boat.setParent(water);
		boat.setParent(loc);
		assertEquals(loc, boat.parent());
		assertEquals(true, boat.isMoored());
	}
}
