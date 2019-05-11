package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.FacilityRegistry;

public class ShopActionTest extends ActionTestBase {
	private ShopAction action;
	private RepairController controller;
	private Shop shop;
	private RepairShop repair;

	@BeforeEach
	public void before() throws ActionException {
		// Create shop
		shop = mock(Shop.class);
		repair = mock(RepairShop.class);
		when(shop.isOpen()).thenReturn(true);
		when(shop.repair()).thenReturn(Optional.of(repair));

		// Create shop registry
		final FacilityRegistry registry = new FacilityRegistry();
		registry.add(loc, shop);

		// Create repair controller
		controller = mock(RepairController.class);
		when(controller.cost()).thenReturn(1);
		when(controller.description(any())).thenReturn("duration");

		// Create action
		action = new ShopAction(registry, controller, ArgumentFormatter.PLAIN);
	}

	private static Shop.Stock stock() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("stock").value(3).build();
		final Shop.Stock stock = new Shop.Stock(1, descriptor, 2);
		return stock;
	}

	@Test
	public void list() throws ActionException {
		final Shop.Stock stock = stock();
		when(shop.list(ObjectDescriptor.Filter.ALL)).thenReturn(Stream.of(stock));
		final Response response = action.list(actor, ObjectDescriptor.Filter.ALL);
		final Response expected = new Response.Builder()
			.add("list.stock.header")
			.add(stock.describe(ArgumentFormatter.PLAIN))
			.build();
		assertEquals(expected, response);
	}

	@Test
	public void listShopNotPresent() throws ActionException {
		action = new ShopAction(new FacilityRegistry(), controller, ArgumentFormatter.PLAIN);
		assertThrows(ActionException.class, () -> action.list(actor, ObjectDescriptor.Filter.ALL));
	}

	@Test
	public void listEmpty() throws ActionException {
		final Response response = action.list(actor, ObjectDescriptor.Filter.ALL);
		assertEquals(Response.of("list.empty.stock"), response);
	}

	@Test
	public void sell() throws ActionException {
		final WorldObject obj = mock(WorldObject.class);
		when(obj.value()).thenReturn(42);
		assertEquals(Response.OK, action.sell(actor, obj));
		verify(shop).sell(obj);
		assertEquals(42, actor.settings().toInteger(PlayerSettings.Setting.CASH));
	}

	@Test
	public void buyIndex() throws ActionException {
		// Add stock
		final Shop.Stock stock = stock();
		when(shop.stock(1)).thenReturn(Optional.of(stock));

		// Add required funds
		actor.settings().set(PlayerSettings.Setting.CASH, 3);

		// Buy
		final Response response = action.buy(actor, 1, 1);
		// TODO - check response

		// Check bought
		verify(shop).buy(stock.descriptor(), 1);
		assertEquals(0, actor.settings().toInteger(PlayerSettings.Setting.CASH));
	}

	@Test
	public void buyIndexInvalid() throws ActionException {
		TestHelper.expect("shop.unknown.index", () -> action.buy(actor, 999, 1));
	}

	@Test
	public void buyInsufficientFunds() throws ActionException {
		when(shop.stock(1)).thenReturn(Optional.of(stock()));
		TestHelper.expect("buy.insufficient.funds", () -> action.buy(actor, 1, 1));
	}

	@Test
	public void buyFilter() throws ActionException {
		// Add stock
		final Shop.Stock stock = stock();
		when(shop.list(ObjectDescriptor.Filter.ALL)).thenReturn(Stream.of(stock));

		// Add required funds
		actor.settings().set(PlayerSettings.Setting.CASH, 3);

		// Buy
		final Response response = action.buy(actor, ObjectDescriptor.Filter.ALL, 1);
		// TODO - check response

		// Check bought
		verify(shop).buy(stock.descriptor(), 1);
		assertEquals(0, actor.settings().toInteger(PlayerSettings.Setting.CASH));
	}

	@Test
	public void buyFilterUnknown() throws ActionException {
		TestHelper.expect("buy.empty.filter", () -> action.buy(actor, ObjectDescriptor.Filter.ALL, 1));
	}

	@Test
	public void buyFilterAmbiguous() throws ActionException {
		final Shop.Stock stock = stock();
		when(shop.list(ObjectDescriptor.Filter.ALL)).thenReturn(Stream.of(stock, stock));
		TestHelper.expect("buy.ambiguous.filter", () -> action.buy(actor, ObjectDescriptor.Filter.ALL, 1));
	}

	@Test
	public void repair() throws ActionException {
		// Create damaged object
		final DurableObject obj = mock(DurableObject.class);
		when(obj.name()).thenReturn("name");
		when(obj.value()).thenReturn(1);
		when(obj.wear()).thenReturn(2);

		// Repair object
		actor.settings().set(PlayerSettings.Setting.CASH, 2);
		final Response response = action.repair(actor, obj);

		// Check response
		final Description expected = new Description.Builder("action.repair").name("name").add("when", "duration").build();
		assertEquals(Response.of(expected), response);

		// Check repaired
		verify(repair).repair(actor, obj, controller);
		assertEquals(0, actor.settings().toInteger(PlayerSettings.Setting.CASH));
	}

	@Test
	public void repairNotPresent() throws ActionException {
		when(shop.repair()).thenReturn(Optional.empty());
		TestHelper.expect("repair.cannot.repair", () -> action.repair(actor, null));
	}

	@Test
	public void repairInsufficientFunds() throws ActionException {
		final DurableObject obj = mock(DurableObject.class);
		when(obj.value()).thenReturn(1);
		when(obj.wear()).thenReturn(2);
		TestHelper.expect("repair.insufficient.funds", () -> action.repair(actor, obj));
	}
}
