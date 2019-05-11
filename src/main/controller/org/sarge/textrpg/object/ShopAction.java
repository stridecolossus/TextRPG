package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.entity.Transaction;
import org.sarge.textrpg.object.Shop.Stock;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.FacilityRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Action to use a {@link Shop}.
 * @author Sarge
 */
@Component
public class ShopAction extends AbstractAction {
	private static final ActionException NONE = ActionException.of("shop.not.found");
	private static final ActionException CLOSED = ActionException.of("shop.closed");

	private final FacilityRegistry registry;
	private final RepairController controller;
	private final ArgumentFormatter money;

// TODO
//	 * < 5 mins		in a few minutes
//	 * < 30 mins	in about half-and-hour
//	 * < 1 hour		in a hour or so
//	 * else			in a few hours

	/**
	 * Constructor.
	 * @param registry		Shop registry
	 * @param controller	Repair controller
	 * @param money			Money argument formatter
	 */
	public ShopAction(FacilityRegistry registry, RepairController controller, @Qualifier(ArgumentFormatter.MONEY) ArgumentFormatter money) {
		this.registry = notNull(registry);
		this.controller = notNull(controller);
		this.money = notNull(money);
	}

	/**
	 * Helper - Finds the shop in the actors location.
	 */
	private Shop find(Entity actor) throws ActionException {
		final Shop shop = registry.find(actor.location(), Shop.class).orElseThrow(() -> NONE);
		if(!shop.isOpen()) throw CLOSED;
		return shop;
	}

	/**
	 * Lists shop stock.
	 * @param actor			Actor
	 * @param filter		Object filter
	 * @return Response
	 * @throws ActionException if there is no shop present in the actors location
	 */
	public Response list(Entity actor, ObjectDescriptor.Filter filter) throws ActionException {
		// Enumerate stock
		final Shop shop = find(actor);
		final List<Description> stock = shop.list(filter).map(entry -> entry.describe(money)).collect(toList());

		// Build response
		if(stock.isEmpty()) {
			return Response.of("list.empty.stock");
		}
		else {
			final Response.Builder response = new Response.Builder();
			response.add("list.stock.header");
			response.add(stock);
			return response.build();
		}
	}

	/**
	 * Sells the given object to a shop.
	 * @param actor			Actor
	 * @param obj			Object to sell
	 * @return Response
	 * @throws ActionException if there is no shop or it does not accept the given type of object
	 */
	public Response sell(PlayerCharacter actor, @Carried(auto=true) WorldObject obj) throws ActionException {
		final Shop shop = find(actor);
		shop.sell(obj);
		actor.settings().modify(PlayerSettings.Setting.CASH, obj.value());
		return Response.OK;
	}

	/**
	 * Buys object(s) from the shop.
	 * @param actor		Actor
	 * @param index		Stock index
	 * @param num		Number of objects to buy
	 * @return Response
	 * @throws ActionException if the index is invalid or the actor does not have sufficient funds
	 */
	public Response buy(PlayerCharacter actor, int index, int num) throws ActionException {
		// Lookup stock by index
		final Shop shop = find(actor);
		final Stock stock = shop.stock(index).orElseThrow(() -> ActionException.of("shop.unknown.index"));

		// Delegate
		return buy(actor, shop, stock, num);
	}

	/**
	 * Buys object(s) matching the given filter from this shop.
	 * @param actor			Actor
	 * @param filter		Filter
	 * @param num			Number to buy
	 * @return Response
	 * @throws ActionException if the shop has insufficient stock or the filter is ambiguous
	 */
	public Response buy(PlayerCharacter actor, ObjectDescriptor.Filter filter, int num) throws ActionException {
		// Lookup stock
		final Shop shop = find(actor);
		final var list = shop.list(filter).collect(toList());
		if(list.isEmpty()) throw ActionException.of("buy.empty.filter");
		if(list.size() != 1) throw ActionException.of("buy.ambiguous.filter");

		// Delegate
		final Stock stock = list.iterator().next();
		return buy(actor, shop, stock, num);
	}

	/**
	 * Buys objects from this shop.
	 * @param actor		Actor
	 * @param shop		Shop
	 * @param stock		Stock
	 * @param num		Number to buy
	 * @return Response
	 * @throws ActionException if the actor does not have sufficient funds
	 */
	private static Response buy(PlayerCharacter actor, Shop shop, Stock stock, int num) throws ActionException {
		// Determine available stock to buy
		final int actual = Math.min(num, stock.number());

		// Check available funds
		final ObjectDescriptor descriptor = stock.descriptor();
		final int cost = actual * descriptor.properties().value();
		final Transaction tx = actor.settings().transaction(PlayerSettings.Setting.CASH, cost, "buy.insufficient.funds");
		tx.check();

		// Buy objects
		shop.buy(descriptor, actual);
		tx.complete();

		// Create objects
		final Stream<WorldObject> objects;
		if(descriptor.isStackable()) {
			objects = Stream.of(new ObjectStack(descriptor, num));
		}
		else {
			objects = IntStream.range(0, actual).mapToObj(n -> descriptor.create());
		}

		// Add to inventory
		final InventoryController controller = new InventoryController("shop.buy");
		final var results = controller.take(actor, objects);

		// Build response
		return Response.of(results);
	}

	/**
	 * Repairs a damaged object.
	 * @param actor		Actor
	 * @param obj		Object to repair
	 * @return Response
	 * @throws ActionException if this shop cannot repair the given object, it is not damaged, or the actor does not have sufficient funds
	 * @see RepairShop
	 */
	public Response repair(PlayerCharacter actor, @Carried(auto=true) DurableObject obj) throws ActionException {
		// Check repair shop
		final Shop shop = find(actor);
		final RepairShop repair = shop.repair().orElseThrow(() -> ActionException.of("repair.cannot.repair"));

		// Check available funds
		final int cost = obj.wear() * controller.cost();
		final Transaction tx = actor.settings().transaction(PlayerSettings.Setting.CASH, cost, "repair.insufficient.funds");
		tx.check();

		// Repair
		final String when = controller.description(obj);
		repair.repair(actor, obj, controller);
		tx.complete();

		// Build response
		final Description response = new Description.Builder("action.repair").name(obj.name()).add("when", when).build();
		return Response.of(response);
	}
}
