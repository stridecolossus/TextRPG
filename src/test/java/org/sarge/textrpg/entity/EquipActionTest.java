package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.Light;
import org.sarge.textrpg.object.LightController;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.PassiveEffect;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.util.ValueModifier;

public class EquipActionTest extends ActionTestBase {
	private EquipAction action;
	private LightController controller;
	private WorldObject obj, two;
	private Light light;

	@BeforeEach
	public void before() throws ActionException {
		// Create equipment with passive effects
		final PassiveEffect passive = new PassiveEffect(mock(ValueModifier.Key.class), 42);
		obj = new ObjectDescriptor.Builder("object").slot(Slot.BACK).passive(passive).build().create();

		// Create a two-handed object
		two = new ObjectDescriptor.Builder("object").slot(Slot.MAIN).twoHanded().build().create();

		// Create light
		light = mock(Light.class);
		when(light.isLightable()).thenReturn(true);

		// Create action
		controller = mock(LightController.class);
		action = new EquipAction(controller);
	}

	@Test
	public void equip() throws ActionException {
		final Description expected = new Description.Builder("equip.object.response")
			.name("object")
			.add("verb", "slot.verb.put")
			.add("place", "slot.place.across")
			.build();
		assertEquals(Response.of(expected), action.equip(actor, obj));
		assertEquals(true, actor.contents().equipment().contains(obj));
		assertEquals(true, actor.contents().equipment().contains(Slot.BACK));
	}

	@Test
	public void equipCannotEquip() throws ActionException {
		obj = ObjectDescriptor.of("invalid").create();
		TestHelper.expect("equip.cannot.equip", () -> action.equip(actor, obj));
	}

	@Test
	public void equipSlotOccupied() throws ActionException {
		final WorldObject other = new ObjectDescriptor.Builder("object").slot(Slot.BACK).build().create();
		actor.contents().equipment().equip(other, Slot.BACK);
		TestHelper.expect("equip.slot.occupied", () -> action.equip(actor, obj));
	}

	@Test
	public void equipAlreadyEquipped() throws ActionException {
		actor.contents().equipment().equip(obj, Slot.BACK);
		TestHelper.expect("equip.already.equipped", () -> action.equip(actor, obj));
	}

	@Test
	public void equipTwoHanded() throws ActionException {
		final Equipment equipment = actor.contents().equipment();
		assertNotNull(action.equip(actor, two));
		assertEquals(true, equipment.contains(two));
		assertEquals(true, equipment.contains(Slot.MAIN));
		assertEquals(true, equipment.contains(Slot.OFF));
	}

	@Test
	public void equipTwoHandedSlotOccupied() throws ActionException {
		actor.contents().equipment().equip(obj, Slot.OFF);
		TestHelper.expect("equip.invalid.twohanded", () -> action.equip(actor, two));
	}

	@Test
	public void equipAutoLight() throws ActionException {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("light").slot(Slot.BELT).build();
		final Light light = new Light.Descriptor(descriptor, Light.Type.DEFAULT, Duration.ofMinutes(42), Percentile.HALF, Percentile.HALF).create();
		final WorldObject tinderbox = new ObjectDescriptor.Builder("tinderbox").category(Light.TINDERBOX).build().create();
		tinderbox.parent(actor);
		action.equip(actor, light);
		verify(controller).light(actor, light);
	}

	@Test
	public void hold() throws ActionException {
		final Equipment equipment = actor.contents().equipment();
		assertNotNull(action.hold(actor, obj));
		assertEquals(true, equipment.contains(obj));
		assertEquals(true, equipment.contains(Slot.MAIN));
	}

	@Test
	public void holdHandsFull() throws ActionException {
		final Equipment equipment = actor.contents().equipment();
		equipment.equip(ObjectDescriptor.of("main").create(), Slot.MAIN);
		equipment.equip(ObjectDescriptor.of("off").create(), Slot.OFF);
		TestHelper.expect("hold.hands.full", () -> action.hold(actor, obj));
	}

	@Test
	public void holdTwoHanded() throws ActionException {
		action.hold(actor, two);
	}

	@Test
	public void holdTwoHandedSlotOccupied() throws ActionException {
		actor.contents().equipment().equip(obj, Slot.OFF);
		TestHelper.expect("equip.invalid.twohanded", () -> action.hold(actor, two));
	}

	@Test
	public void remove() throws ActionException {
		final Equipment equipment = actor.contents().equipment();
		equipment.equip(obj, Slot.BACK);
		assertNotNull(action.remove(actor, obj));
		assertEquals(false, equipment.contains(Slot.BACK));
	}

	@Test
	public void removeNotEquipped() throws ActionException {
		TestHelper.expect("remove.not.equipped", () -> action.remove(actor, obj));
	}

	@Test
	public void removeCannotEquip() throws ActionException {
		obj = ObjectDescriptor.of("object").create();
		TestHelper.expect("remove.cannot.equip", () -> action.remove(actor, obj));
	}
}
