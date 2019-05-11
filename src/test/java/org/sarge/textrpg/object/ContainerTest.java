package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents.Limit;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.util.TestHelper;

public class ContainerTest {
	private Container container;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		container = new Container(new Container.Descriptor(ObjectDescriptor.of("container"), "in", LimitsMap.EMPTY));
		obj = new ObjectDescriptor.Builder("object").category("cat").weight(42).slot(Slot.KEYRING).build().create();
	}

	@Test
	public void constructor() {
		assertEquals("container", container.name());
		assertNotNull(container.descriptor());
		assertNotNull(container.contents());
		assertEquals(0, container.weight());
		assertEquals(true, container.isOpen());
		assertEquals(Contents.EnumerationPolicy.DEFAULT, container.contents().policy());
		assertEquals("in", container.contents().placement());
	}

	@Test
	public void reason() {
		obj.parent(TestHelper.parent());
		assertEquals(Optional.empty(), container.contents().reason(obj));
	}

	@Test
	public void categoryLimit() {
		final Limit limit = Container.categoryLimit(Set.of("cat"));
		assertEquals(true, limit.accepts(null, obj));
		assertEquals(false, limit.accepts(null, ObjectDescriptor.of("invalid").create()));
	}

	@Test
	public void slotLimit() {
		final Limit limit = Container.slotLimit(Slot.KEYRING);
		assertEquals(true, limit.accepts(null, obj));
		assertEquals(false, limit.accepts(null, ObjectDescriptor.of("invalid").create()));
	}

	@Test
	public void slotLimitNone() {
		assertThrows(IllegalArgumentException.class, () -> Container.slotLimit(Slot.NONE));
	}

	@Test
	public void slotLimitInvalidSlot() {
		assertThrows(IllegalArgumentException.class, () -> Container.slotLimit(Slot.ARMS));
	}
}
