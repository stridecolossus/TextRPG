package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

public class InventoryTest {
	private Inventory inv;

	@BeforeEach
	public void before() {
		inv = new Inventory();
	}

	@Test
	public void constructor() {
		assertEquals(true, inv.isEmpty());
		assertEquals(Contents.EnumerationPolicy.NONE, inv.policy());
		assertNotNull(inv.equipment());
	}

	@Test
	public void reason() {
		final Thing thing = ObjectDescriptor.of("object").create();
		assertEquals(Optional.empty(), inv.reason(thing));
	}

	@Test
	public void find() {
		final WorldObject obj = mock(WorldObject.class);
		inv.add(obj);
		assertEquals(Optional.of(obj), inv.find(t -> t == obj));
	}

	@Test
	public void findNotFound() {
		assertEquals(Optional.empty(), inv.find(t -> true));
	}

	@Test
	public void findRecursive() {
		final Container container = new Container.Descriptor(ObjectDescriptor.of("container"), "in", LimitsMap.EMPTY).create();
		final WorldObject obj = ObjectDescriptor.of("object").create();
		obj.parent(container);
		inv.add(container);
		assertEquals(Optional.of(obj), inv.find(t -> t == obj));
	}

	@Test
	public void container() {
		final Container container = new Container.Descriptor(ObjectDescriptor.of("container"), "in", LimitsMap.EMPTY).create();
		inv.add(container);
		final WorldObject obj = mock(WorldObject.class);
		assertEquals(Optional.of(container), inv.container(obj));
	}

	@Test
	public void containerNoneMatched() {
		final LimitsMap limits = new LimitsMap(Map.of("reason", (c, obj) -> false));
		final Container container = new Container.Descriptor(ObjectDescriptor.of("container"), "in", limits).create();
		inv.add(container);
		final WorldObject obj = mock(WorldObject.class);
		assertEquals(Optional.empty(), inv.container(obj));
	}

	@Test
	public void containerEmptyContainers() {
		assertEquals(Optional.empty(), inv.container(mock(WorldObject.class)));
	}
}
