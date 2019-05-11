package org.sarge.textrpg.object;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// TODO
public class ObjectManagerTest {
	private WorldObject obj;

	@BeforeEach
	public void before() {
		obj = new WorldObject(ObjectDescriptor.of("object"));
	}

	@Test
	public void container() {
//		// Create container
//		final var container = mock(Container.class);
//		when(container.contents()).thenReturn(contents);
//
//		// Add some contents
//		contents.add(obj);
//
//		// Update and check contents replenished
//		final var factory = mock(LootFactory.class);
//		final var manager = ObjectManager.of(container, factory);
//		manager.update();
//		assertEquals(0, contents.size());
//		verify(factory).generate(null);
	}

	@Test
	public void location() {
//		// Create location
//		final var loc = mock(WorldLocation.class);
//		when(loc.objects()).thenReturn(contents);
//
//		// Add some contents
//		contents.add(obj);
//
//		// Update and check contents replenished
//		final var manager = ObjectManager.of(loc, obj.descriptor(), 2);
//		manager.update();
//		assertEquals(2, contents.size());
	}
}
