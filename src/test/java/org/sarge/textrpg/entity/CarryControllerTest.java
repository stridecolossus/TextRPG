package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.CarryController.Result;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Description;

public class CarryControllerTest extends ActionTestBase {
	private CarryController controller;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		obj = new ObjectDescriptor.Builder("object").size(Size.MEDIUM).build().create();
		controller = new CarryController();
	}

	/**
	 * Check dropped result.
	 */
	private void drop(Result result) {
		result.apply(obj, actor);
		assertEquals(actor.location(), obj.parent());
	}

	/**
	 * Check dropped result.
	 */
	private void check(String expected) {
		final Result result = controller.carry(actor, obj);
		assertEquals(false, result.isCarried());
		assertEquals(expected, result.message());
		drop(result);
	}

	@Test
	public void carryFixture() {
		obj = ObjectDescriptor.fixture("fixture").create();
		check("invalid.fixture");
	}

	@Test
	public void carryTooLarge() {
		obj = new ObjectDescriptor.Builder("object").size(Size.LARGE).build().create();
		check("too.large");
	}

	@Test
	public void carryTooHeavy() {
		obj = new ObjectDescriptor.Builder("object").weight(1).build().create();
		check("too.heavy");
	}

	@Test
	public void carryAlreadyCarried() {
		obj.parent(actor);
		check("already.carried");
	}

	@Test
	public void carryInventoryReason() {
		final Inventory inv = new Inventory() {
			@Override
			public Optional<String> reason(Thing thing) {
				return Optional.of("reason");
			}
		};
		when(actor.contents()).thenReturn(inv);
		check("reason");
	}

	@Test
	public void carryContainer() {
		// Add a container
		final Container container = new Container.Descriptor(ObjectDescriptor.of("container"), "in", LimitsMap.EMPTY).create();
		actor.contents().add(container);

		// Carry object
		final Result result = controller.carry(actor, obj);
		assertEquals(true, result.isCarried());
		assertEquals("object.container", result.message());

		// Check description
		final Description.Builder builder = new Description.Builder("key");
		result.describe(builder);
		assertEquals("container", builder.get("container").argument());

		// Check added to container
		result.apply(obj, actor);
		assertEquals(container, obj.parent());
	}

	@Test
	public void carryHeldMainHand() {
		// Carry object
		final Result result = controller.carry(actor, obj);
		assertEquals(true, result.isCarried());
		assertEquals("object.carried", result.message());

		// Check held in main-hand
		result.apply(obj, actor);
		assertEquals(actor, obj.parent());
		assertEquals(obj, actor.contents().equipment().equipment().get(Slot.MAIN));
	}

	@Test
	public void carryHeldOffHand() {
		// Use main-hand
		actor.contents().equipment().equip(obj, Slot.MAIN);

		// Carry object
		final Result result = controller.carry(actor, obj);
		assertEquals(true, result.isCarried());
		assertEquals("object.carried", result.message());

		// Check held in main-hand
		result.apply(obj, actor);
		assertEquals(actor, obj.parent());
		assertEquals(obj, actor.contents().equipment().equipment().get(Slot.OFF));
	}

	@Test
	public void carryCannotCarry() {
		// Use both hands
		final Equipment equipment = actor.contents().equipment();
		equipment.equip(obj, Slot.MAIN);
		equipment.equip(obj, Slot.OFF);

		// Carry object
		final Result result = controller.carry(actor, obj);
		assertEquals(false, result.isCarried());
		assertEquals("cannot.carry", result.message());
		drop(result);
	}
}
