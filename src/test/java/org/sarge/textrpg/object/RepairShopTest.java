package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class RepairShopTest {
	private static final String CATEGORY = "cat";

	private RepairShop shop;
	private DurableObject object;
	private Actor actor;
	private RepairController controller;

	@BeforeEach
	public void before() {
		shop = new RepairShop(Set.of(CATEGORY));
		actor = mock(Actor.class);
		object = new DurableObject(new DurableObject.Descriptor(new ObjectDescriptor.Builder("object").category(CATEGORY).build(), 1));
		object.parent(TestHelper.parent());
		controller = mock(RepairController.class);
	}

	@Test
	public void repair() throws ActionException {
		object.use();
		shop.repair(actor, object, controller);
		assertEquals(false, object.isAlive());
		verify(controller).repair(eq(object), any());
	}

	@Test
	public void repairNotDamaged() throws ActionException {
		TestHelper.expect("repair.not.damaged", () -> shop.repair(actor, object, controller));
	}

	@Test
	public void repairedNone() throws ActionException {
		final var repaired = shop.repaired(actor);
		assertNotNull(repaired);
		assertEquals(0, repaired.count());
	}
}
