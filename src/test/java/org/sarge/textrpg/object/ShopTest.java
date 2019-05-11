package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.object.Shop.Stock;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class ShopTest {
	private static final String CATEGORY = "cat";

	private Shop shop;
	private ObjectDescriptor descriptor;

	@BeforeEach
	public void before() {
		descriptor = new ObjectDescriptor.Builder("object").value(42).category(CATEGORY).build();
		shop = new Shop(List.of(new Stock(1, descriptor, 1)), Set.of(CATEGORY), mock(RepairShop.class));
	}

	@Test
	public void constructor() {
		assertEquals(false, shop.isOpen());
		assertNotNull(shop.repair());
		assertTrue(shop.repair().isPresent());
	}

	@Test
	public void open() {
		shop.setOpen(true);
		assertEquals(true, shop.isOpen());
	}

	@Test
	public void describeStock() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("object").value(3).build();
		final Shop.Stock stock = new Shop.Stock(1, descriptor, 2);
		final Description expected = new Description.Builder("shop.stock")
			.add("index", 1)
			.add("name", "object")
			.add("number", 2)
			.add("price", 3, ArgumentFormatter.PLAIN)
			.build();
		assertEquals(expected, stock.describe(ArgumentFormatter.PLAIN));
	}

	@Test
	public void invalidStockValue() {
		assertThrows(IllegalArgumentException.class, () -> new Shop.Stock(1, ObjectDescriptor.of("zero"), 2));
	}

	@Test
	public void list() {
		assertNotNull(shop.list(ObjectDescriptor.Filter.ALL));
		assertArrayEquals(new Stock[]{new Stock(1, descriptor, 1)}, shop.list(ObjectDescriptor.Filter.ALL).toArray());
	}

	@Test
	public void listFilter() {
		final ObjectDescriptor.Filter filter = ObjectDescriptor.Filter.of(descriptor);
		assertArrayEquals(new Stock[]{new Stock(1, descriptor, 1)}, shop.list(filter).toArray());
	}

	@Test
	public void listEmpty() {
		final ObjectDescriptor.Filter filter = desc -> false;
		assertEquals(0, shop.list(filter).count());
	}

	@Test
	public void sell() throws ActionException {
		final WorldObject obj = descriptor.create();
		obj.parent(TestHelper.parent());
		shop.sell(obj);
		assertArrayEquals(new Stock[]{new Stock(1, descriptor, 2)}, shop.list(ObjectDescriptor.Filter.ALL).toArray());
		assertEquals(false, obj.isAlive());
	}

	@Test
	public void sellNotTraded() throws ActionException {
		final WorldObject obj = new WorldObject(ObjectDescriptor.of("other"));
		TestHelper.expect("shop.buy.invalid", () -> shop.sell(obj));
	}

	@Test
	public void sellDamaged() throws ActionException {
		final WorldObject obj = new WorldObject(descriptor) {
			@Override
			public boolean isDamaged() {
				return true;
			}
		};
		TestHelper.expect("shop.buy.damaged", () -> shop.sell(obj));
	}

	@Test
	public void stock() {
		final Stock stock = shop.stock(1).get();
		assertEquals(descriptor, stock.descriptor());
		assertEquals(1, stock.number());
	}

	@Test
	public void stockUnknown() {
		assertEquals(Optional.empty(), shop.stock(2));
	}

	@Test
	public void buy() throws ActionException {
		shop.buy(descriptor, 1);
		assertEquals(Optional.empty(), shop.stock(1));
	}

	@Test
	public void buyEmptyStock() throws ActionException {
		shop.buy(descriptor, 1);
		TestHelper.expect("shop.stock.empty", () -> shop.buy(descriptor, 1));
	}

	@Test
	public void reset() throws ActionException {
		// Add an object that is not part of the initial stock
		final WorldObject obj = new WorldObject(new ObjectDescriptor.Builder("other").category(CATEGORY).build());
		obj.parent(TestHelper.parent());
		shop.sell(obj);

		// Remove some of the stock
		shop.buy(descriptor, 1);

		// Reset and verify stock has been restored
		shop.reset();
		list();
	}
}
