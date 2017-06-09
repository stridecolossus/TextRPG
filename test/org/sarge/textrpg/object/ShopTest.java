package org.sarge.textrpg.object;

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
import org.sarge.textrpg.object.DurableObject.Descriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.object.Shop.StockEntry;

public class ShopTest extends ActionTest {
	private Shop shop;
	private Descriptor descriptor;
	
	@Before
	public void before() {
		descriptor = new Descriptor(new Builder("durable").category("cat").build(), 1);
		shop = new Shop(Collections.singleton("cat"), Collections.singletonMap(descriptor, 1), 2, 3, 4);
		shop.setOpen(true);
	}
	
	@After
	public void after() {
		Shop.QUEUE.reset();
	}
	
	@Test
	public void list() throws ActionException {
		assertNotNull(shop.list(ObjectFilter.ALL));
		assertEquals(1, shop.list(ObjectFilter.ALL).count());
		final StockEntry entry = shop.list(ObjectFilter.ALL).iterator().next();
		assertEquals(descriptor, entry.getDescriptor());
		assertEquals(1, entry.getIndex());
		assertEquals(1, entry.getCount());
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
		obj.wear();
		expect("sell.damaged.object");
		shop.sell(obj);
	}
	
	@Test
	public void calculateRepairCost() throws ActionException {
		final DurableObject obj = descriptor.create();
		obj.wear();
		assertEquals(3, shop.calculateRepairCost(obj));
	}
	
	@Test
	public void repair() throws ActionException {
		// Repair object and check removed from inventory
		final DurableObject obj = descriptor.create();
		obj.wear();
		final long duration = shop.repair(actor, obj);
		assertEquals(Thing.LIMBO, obj.getParent());
		assertEquals(0, shop.getRepaired(actor).count());
		assertEquals(2 * 3, duration);
		
		// Wait for repair event and check now ready
		assertEquals(2, Shop.QUEUE.stream().count());
		Shop.QUEUE.update(duration);
		final List<WorldObject> results = shop.getRepaired(actor).collect(toList());
		assertEquals(1, results.size());
		assertEquals(obj, results.iterator().next());
		assertEquals(1, Shop.QUEUE.stream().count());
	}
	
	@Test
	public void repairDiscarded() throws ActionException {
		final DurableObject obj = descriptor.create();
		obj.wear();
		final long duration = shop.repair(actor, obj);
		Shop.QUEUE.update(duration + 6L);
		assertEquals(0, shop.getRepaired(actor).count());
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
		assertEquals("shop", topic.getName());
		assertNotNull(topic.getScript());
		assertEquals(shop, topic.getShop());
	}
}
