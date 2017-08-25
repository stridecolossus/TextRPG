package org.sarge.textrpg.world;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.world.Area.Ambient;
import org.sarge.textrpg.world.Area.Resource;

public class AreaTest {
	private Area parent, area;
	private LootFactory herbs;
	private Ambient ambient;

	@Before
	public void before() {
		parent = new Area("parent", Area.ROOT, Collections.emptyMap(), Collections.emptyList());
		herbs = mock(LootFactory.class);
		ambient = new Ambient("ambient", 42, true);
		area = new Area("area", parent, Collections.singletonMap(Resource.HERBS, herbs), Collections.singleton(ambient));
	}

	@Test
	public void constructor() {
		assertEquals("area", area.getName());
		assertEquals(parent, area.getParent());
		assertEquals(Optional.of(herbs), area.getResource(Resource.HERBS));
		assertNotNull(area.getAmbientEvents());
		assertArrayEquals(new Ambient[]{ambient}, area.getAmbientEvents().toArray());
	}

	@Test
	public void path() {
		assertArrayEquals(new Area[]{area, parent}, area.path().toArray());
	}
}
