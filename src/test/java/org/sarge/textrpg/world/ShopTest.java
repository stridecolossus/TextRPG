package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.object.DurableObject;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectFilter;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.object.DurableObject.Descriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.world.RepairShop;
import org.sarge.textrpg.world.Shop;
import org.sarge.textrpg.world.Shop.StockEntry;

public class ShopTest extends ActionTest {
	private Shop shop;
	private RepairShop repair;
	private Descriptor descriptor;

	@Before
	public void before() {
		descriptor = new Descriptor(new Builder("durable").category("cat").build(), 1);
		repair = new RepairShop(2L, 3, 4L);
		shop = new Shop(Collections.singleton("cat"), Collections.singletonMap(descriptor, 1), repair);
		shop.setOpen(true);
	}

	@After
	public void after() {
		repair.getEventQueue().reset();
	}

	@Test
	public void list() throws ActionException {
		assertNotNull(shop.list(ObjectFilter.ALL));
		assertEquals(1, shop.list(ObjectFilter.ALL).count());
		final StockEntry entry = shop.list(ObjectFilter.ALL).iterator().next();
		assertEquals(descriptor, entry.descriptor());
		assertEquals(1, entry.index());
		assertEquals(1, entry.count());
	}

	@Test
	public void buy() throws ActionException {
		shop.buy(descriptor);
		assertEquals(0, shop.list(ObjectFilter.ALL).count());
	}

	@Test
	public void buyInvalidStock() throws ActionException {
		expect("buy.invalid.stock");
		shop.buy(new ObjectDescriptor("unknown"));
	}

	@Test
	public void buyEmptyStock() throws ActionException {
		shop.buy(descriptor);
		expect("buy.empty.stock");
		shop.buy(descriptor);
	}

	@Test
	public void sell() throws ActionException {
		final WorldObject obj = descriptor.create();
		shop.sell(obj);
	}

	@Test
	public void sellInvalid() throws ActionException {
		final WorldObject obj = new ObjectDescriptor("invalid").create();
		expect("sell.invalid.object");
		shop.sell(obj);
	}

	@Test
	public void sellDamaged() throws ActionException {
		final DurableObject obj = descriptor.create();
		obj.use();
		expect("sell.damaged.object");
		shop.sell(obj);
	}

	@Test
	public void calculateRepairCost() throws ActionException {
		final DurableObject obj = descriptor.create();
		obj.use();
		assertEquals(3, repair.calculateRepairCost(obj));
	}

	@Test
	public void repair() throws ActionException {
		// Repair object and check removed from inventory
		final DurableObject obj = descriptor.create();
		obj.use();
		final long duration = shop.repair(actor, obj);
		assertEquals(Thing.LIMBO, obj.parent());
		assertEquals(0, repair.getRepaired(actor).count());
		assertEquals(2 * 3, duration);

		// Wait for repair event and check now ready
		assertEquals(2, repair.getEventQueue().size());
		repair.getEventQueue().execute(duration);
		final List<WorldObject> results = repair.getRepaired(actor).collect(toList());
		assertEquals(1, results.size());
		assertEquals(obj, results.iterator().next());
		assertEquals(1, repair.getEventQueue().size());
	}

	@Test
	public void repairDiscarded() throws ActionException {
		final DurableObject obj = descriptor.create();
		obj.use();
		final long duration = shop.repair(actor, obj);
		repair.getEventQueue().execute(duration + 6L);
		assertEquals(0, repair.getRepaired(actor).count());
	}

	@Test
	public void repairNotDamaged() throws ActionException {
		final DurableObject obj = descriptor.create();
		expect("repair.not.damaged");
		shop.repair(actor, obj);
	}

	@Test
	public void closed() throws ActionException {
		shop.setOpen(false);
		expect("shop.closed");
		shop.list(ObjectFilter.ALL);
	}

	@Test
	public void topic() {
		final Topic topic = shop.topic("text");
		assertNotNull(topic);
		assertEquals("shop", topic.name());
		assertNotNull(topic.script());
		assertEquals(shop, topic.shop());
	}
}
