package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Description;

public class InventoryControllerTest extends ActionTestBase {
	private InventoryController controller;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		obj = ObjectDescriptor.of("object").create();
		controller = new InventoryController("prefix");
	}

	@Test
	public void take() {
		// Take object
		final var results = controller.take(actor, Stream.of(obj));
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(actor, obj.parent());

		// Check description
		final Description expected = new Description.Builder("prefix.object.carried").name("object").build();
		assertEquals(expected, results.iterator().next());
	}

	@DisplayName("Object that cannot be carried is dropped to actors location")
	@Test
	public void takeDropped() {
		obj = new ObjectDescriptor.Builder("object").size(Size.HUGE).build().create();
		controller.take(actor, Stream.of(obj));
		assertEquals(actor.location(), obj.parent());
	}
}
