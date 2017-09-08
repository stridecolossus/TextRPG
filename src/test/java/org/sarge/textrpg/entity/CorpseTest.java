package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.entity.Race.Builder;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

public class CorpseTest extends ActionTest {
	private Corpse corpse;
	private Race race;
	private WorldObject obj;
	private LootFactory butcher;
	
	@Before
	public void before() {
		// Create an object
		obj = mock(WorldObject.class);
		when(obj.weight()).thenReturn(2);
		
		// Create butchery loot
		butcher = mock(LootFactory.class);
		when(butcher.generate(actor)).thenReturn(Stream.of(obj));
		
		// Create corpse
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("corpse").weight(3).build();
		race = new Builder("race").butcherFactory(butcher).build();
		corpse = new Corpse(descriptor, race, Collections.singleton(mock(WorldObject.class)));
	}
	
	@Test
	public void constructor() {
		assertEquals("corpse", corpse.name());
		assertEquals(3, corpse.weight());
		assertNotNull(corpse.contents());
		assertEquals(1, corpse.contents().size());
		assertEquals(false, corpse.isButchered());
	}
	
	@Test
	public void butcher() throws ActionException {
		corpse.butcher(actor);
		assertEquals(true, corpse.isButchered());
		verify(butcher).generate(actor);
		assertEquals(2, corpse.contents().size());
		assertEquals(3 + 2, corpse.weight());
	}

	@Test(expected = IllegalStateException.class)
	public void butcherAlreadyButchered() throws ActionException {
		corpse.butcher(actor);
		corpse.butcher(actor);
	}
	
	@Test
	public void butcherCannotButcher() throws ActionException {
		race = new Builder("race").butcherFactory(null).build();
		corpse = new Corpse(new ObjectDescriptor("corpse"), race, Collections.singleton(obj));
		expect("corpse.cannot.butcher");
		corpse.butcher(actor);
	}
}
