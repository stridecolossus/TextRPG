package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.Furniture.Descriptor;

public class FurnitureTest {
	private Furniture furniture;

	@Before
	public void before() {
		furniture = new Furniture(new Descriptor(new ContentsObjectDescriptor(new ObjectDescriptor("chair"), Collections.emptyMap()), Collections.singleton(Stance.RESTING)));
	}

	@Test
	public void constructor() {
		assertEquals(true, furniture.isSentient());
		assertNotNull(furniture.getContents());
		assertEquals("furniture", furniture.getParentName());
	}

	@Test
	public void isValid() {
		assertEquals(true, furniture.getDescriptor().isValid(Stance.RESTING));
	}
}
