package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractActiveAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectFilter;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.world.RepairShop;
import org.sarge.textrpg.world.Shop;
import org.sarge.textrpg.world.Shop.StockEntry;

/**
 * Action to interact with a {@link Shop}.
 * @author Sarge
 */
public class ShopAction extends AbstractActiveAction {
	/**
	 * Shop operation.
	 */
	public enum Operation {
		LIST,
		BUY,
		SELL,
		COST,
		REPAIR,
	}

	private final Operation op;

	/**
	 * Constructor.
	 * @param op Shop operation
	 */
	public ShopAction(Operation op) {
		super(op.name());
		Check.notNull(op);
		this.op = op;
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}

	/**
	 * List all stock.
	 */
	public ActionResponse list(Entity actor) throws ActionException {
		if(op != Operation.LIST) return INVALID;
		return execute(actor, ObjectFilter.ALL);
	}

	/**
	 * List available stock or buy specified object.
	 * @param ctx
	 * @param actor
	 * @param str
	 * @throws ActionException
	 */
	public ActionResponse execute(Entity actor, ObjectFilter filter) throws ActionException {
		// List stock
		final Shop shop = find(actor);
		final Stream<StockEntry> stock = shop.list(filter);

		// Delegate
		switch(op) {
		case LIST:
			// Notify stock
			final List<Description> list = stock.map(ShopAction::describe).collect(toList());
			return new ActionResponse(Description.create("shop.list", list));

		case BUY:
			// Buy object from shop
			final List<StockEntry> results = stock.collect(toList());
			if(results.isEmpty()) throw new ActionException("buy.unknown.object");
			if(results.size() != 1) throw new ActionException("buy.ambiguous.object");
			return buy(actor, results.iterator().next().descriptor(), shop);

		default:
			return INVALID;
		}
	}

	/**
	 * Describes a stock-entry.
	 */
	private static Description describe(StockEntry entry) {
		return new Description.Builder("list.entry")
			.wrap("name", entry.descriptor().getName())
			.add("index", entry.index())
			.add("count", entry.count())
			.build();
	}

	/**
	 * Buys an object by index.
	 */
	public ActionResponse buy(Entity actor, Integer index) throws ActionException {
		final Shop shop = find(actor);
		if(op != Operation.BUY) return INVALID;
		final ObjectDescriptor descriptor = shop.descriptor(index).orElseThrow(() -> new ActionException("buy.invalid.index"));
		return buy(actor, descriptor, shop);
	}

	/**
	 * Buys an object from the shop.
	 */
	private ActionResponse buy(Entity actor, ObjectDescriptor descriptor, Shop shop) throws ActionException {
		// Check available funds
		final Entity player = actor;
		final int value = descriptor.getProperties().getValue();
		if(player.values().get(EntityValue.CASH) < value) throw new ActionException("buy.insufficient.funds");

		// Buy from shop
		shop.buy(descriptor);
		player.modify(EntityValue.CASH, -value);

		// Create object and add to inventory
		final WorldObject obj = descriptor.create();
		obj.setParent(actor);

		// Build response
		return response(obj);
	}

	/**
	 * Sell or repair.
	 */
	public ActionResponse execute(Entity actor, WorldObject obj) throws ActionException {
		final Shop shop = find(actor);
		verifyCarried(actor, obj);
		switch(op) {
		case SELL:
			// Sell object to shop
			shop.sell(obj);
			actor.modify(EntityValue.CASH, obj.value());
			return response(obj);

		case COST:
			// Calculate repair cost
			final int amount = getRepairShop(shop).calculateRepairCost(obj);
			return new ActionResponse(new Description("repair.cost", "amount", amount));

		case REPAIR:
			// Consume repair cost
			final int cost = getRepairShop(shop).calculateRepairCost(obj);
			if(actor.values().get(EntityValue.CASH) < cost) throw new ActionException("repair.insufficient.funds");
			actor.modify(EntityValue.CASH, -cost);

			// Notify expected duration
			final long duration = shop.repair(actor, obj);
			return new ActionResponse(new Description("repair.duration", "duration", duration));

		default:
			return INVALID;
		}
	}

	/**
	 * Finds a shop in the current location.
 	 */
	private static Shop find(Entity actor) throws ActionException {
		return ActionHelper.findTopic(actor.location(), Shop.TOPIC_NAME).map(Topic::shop).orElseThrow(() -> new ActionException("shop.not.found"));
	}

	/**
	 * Gets the repair facility at this shop.
	 * @throws ActionException if this shop does not repair
	 */
	private static RepairShop getRepairShop(Shop shop) throws ActionException {
	    return shop.repairShop().orElseThrow(() -> new ActionException("shop.cannot.repair"));
	}
}
