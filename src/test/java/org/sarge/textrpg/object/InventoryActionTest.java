package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Inventory;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;

public class InventoryActionTest {
	private InventoryAction action;
	private Entity actor;

	@BeforeEach
	public void before() {
		final Inventory inv = new Inventory();
		actor = mock(Entity.class);
		when(actor.contents()).thenReturn(inv);
		action = new InventoryAction(new ArgumentFormatter.Registry());
	}

	/**
	 * Equips an object.
	 */
	private void equip(Slot slot) {
		final WorldObject obj = new ObjectDescriptor.Builder("object").slot(slot).build().create();
		actor.contents().equipment().equip(obj, slot);
	}

	/**
	 * Executes and checks the expected response.
	 */
	private void check(Description expected) {
		assertEquals(Response.of(List.of(new Description("list.inventory.header"), expected)), action.inventory(actor));
	}

	@Test
	public void equipment() {
		final Description expected = new Description.Builder("list.inventory.equipment")
			.name("object")
			.add("verb", "equip.verb.put")
			.add("placement", "slot.place.across")
			.add("slot", "slot.back")
			.build();
		equip(Slot.BACK);
		check(expected);
	}

	@Test
	public void wielded() {
		final Description expected = new Description.Builder("list.inventory.short")
			.name("object")
			.add("verb", "equip.verb.wielded")
			.build();
		equip(Slot.MAIN);
		check(expected);
	}

	@Test
	public void held() {
		final Description expected = new Description.Builder("list.inventory.short")
			.name("object")
			.add("verb", "equip.verb.held")
			.build();
		equip(Slot.OFF);
		check(expected);
	}

	@Test
	public void empty() {
		assertEquals(Response.of("list.inventory.empty"), action.inventory(actor));
	}
}
