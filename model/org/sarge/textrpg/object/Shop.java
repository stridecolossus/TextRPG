package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Pair;
import org.sarge.lib.util.StrictSet;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.DefaultTopic;
import org.sarge.textrpg.common.Event;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Topic;

/**
 * Shop model.
 * @author Sarge
 */
public class Shop {
	/**
	 * Shop repair event queue.
	 */
	public static final EventQueue QUEUE = new EventQueue();
	
	/**
	 * Stock entry.
	 */
	public class StockEntry {
		private final ObjectDescriptor descriptor;
		private final int idx;
		private final int count;
		
		private StockEntry(ObjectDescriptor descriptor) {
			this.descriptor = descriptor;
			this.idx = index.indexOf(descriptor) + 1;
			this.count = stock.get(descriptor).current;
		}

		/**
		 * @return Descriptor
		 */
		public ObjectDescriptor getDescriptor() {
			return descriptor;
		}

		/**
		 * @return Shop index
		 */
		public int getIndex() {
			return idx;
		}
		
		/**
		 * @return Number in stock
		 */
		public int getCount() {
			return count;
		}
	}
	
	/**
	 * Stock level.
	 */
	private class StockLevel {
		private final int initial;
		private int current;
		
		public StockLevel(int num) {
			this.initial = num;
			this.current = num;
		}
		
		void reset() {
			current = initial;
		}
	}
	
	/**
	 * Topic name for a shop.
	 */
	public static final String TOPIC_NAME = "shop";

	/**
	 * Creates a conversation topic for this shop.
	 * @param text Shop description key
	 * @return Shop topic
	 */
	public Topic topic(String text) {
		return new DefaultTopic(TOPIC_NAME, Script.message(text)) {
			@Override
			public Shop getShop() {
				return Shop.this;
			}
		};
	}

	private final long duration;
	private final int mod;
	private final long discard;
	private final Set<String> accepts;
	private final Map<ObjectDescriptor, StockLevel> stock;
	private final List<ObjectDescriptor> index;
	private final List<Pair<Actor, WorldObject>> repaired = new ArrayList<>();
	
	private boolean open;
	
	/**
	 * Constructor.
	 * @param accepts		Object categories that this shop will purchase
	 * @param stock			Initial stock
	 * @param duration		Repair duration multiplier (ms)
	 * @param mod			Repair cost multiplier
	 * @param discard		Discard period (ms)
	 */
	public Shop(Set<String> accepts, Map<ObjectDescriptor, Integer> stock, long duration, int mod, long discard) {
		Check.oneOrMore(duration);
		Check.oneOrMore(mod);
		Check.oneOrMore(discard);
		this.accepts = new StrictSet<>(accepts);
		this.stock = stock.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> new StockLevel(e.getValue())));
		this.index = new ArrayList<ObjectDescriptor>(stock.keySet());
		this.duration = duration;
		this.mod = mod;
		this.discard = discard;
	}
	
	/**
	 * @return Whether the shop is open
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Open/closes this shop.
	 * @param open Whether open or closed
	 */
	public void setOpen(boolean open) {
		assert this.open != open;
		this.open = open;
	}
	
	/**
	 * @throws ActionException if this shop is closed
	 */
	private void checkOpen() throws ActionException {
		if(!open) throw new ActionException("shop.closed");
	}

	/**
	 * Lists objects for sale matching the given filter.
	 * @param filter Filter
	 * @return Objects for sale mapped
	 * @throws ActionException if the shop is closed
	 */
	public Stream<StockEntry> list(ObjectFilter filter) throws ActionException {
		checkOpen();
		return stock.keySet().stream().filter(filter.predicate()).map(StockEntry::new).filter(e -> e.count > 0);
	}

	/**
	 * Retrieves an object descriptor by index.
	 * @param idx Index
	 * @return Object descriptor
	 */
	public Optional<ObjectDescriptor> getDescriptor(int idx) {
		return Optional.ofNullable(index.get(idx));
	}
	
	/**
	 * Buys an object <b>from</b> this shop.
	 * @param descriptor Object to buy
	 * @throws ActionException if this shop does not stock the given object
	 */
	public void buy(ObjectDescriptor descriptor) throws ActionException {
		// Check available stock
		checkOpen();
		final StockLevel entry = stock.get(descriptor);
		if(entry == null) throw new ActionException("buy.invalid.stock");
		if(entry.current == 0) throw new ActionException("buy.empty.stock");

		// Remove from stock
		--entry.current;
	}
	
	/**
	 * Sells an object <b>to</b> this shop.
	 * @param obj Object to sell
	 * @throws ActionException
	 */
	public void sell(WorldObject obj) throws ActionException {
		// Check shop accepts this object
		checkOpen();
		final ObjectDescriptor descriptor = obj.getDescriptor();
		accepts(descriptor, "sell");
		
		// Check not damaged
		if(obj.isDamaged()) {
			throw new ActionException("sell.damaged.object");
		}

		// Remove from inventory
		obj.destroy();

		// Add to stock if shop also sells this object
		final StockLevel entry = stock.get(descriptor);
		if(entry != null) {
			++entry.current;
		}
	}

	/**
	 * Checks that this shop accepts the given object.
	 */
	private void accepts(ObjectDescriptor descriptor, String reason) throws ActionException {
		if(!descriptor.getCharacteristics().getCategories().anyMatch(accepts::contains)) {
			throw new ActionException(reason + ".invalid.object");
		}
	}

	/**
	 * Calculates the cost of repairing the given object.
	 * @param obj Damaged object
	 * @return Repair cost
	 */
	public int calculateRepairCost(WorldObject obj) {
		if(obj instanceof DurableObject) {
			final DurableObject durable = (DurableObject) obj;
			return durable.getWear() * mod;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Repairs a damaged object.
	 * @param obj		Damaged object
	 * @param actor		Actor
	 * @return Repair duration (ms)
	 * @throws ActionException
	 */
	public long repair(Actor actor, WorldObject obj) throws ActionException {
		// Check can be repaired by this shop
		checkOpen();
		accepts(obj.getDescriptor(), "repair");
		final int cost = calculateRepairCost(obj);
		if(cost == 0) throw new ActionException("repair.not.damaged");

		// Remove from inventory
		obj.destroy();
		
		// Generate repair event
		final Pair<Actor, WorldObject> entry = new Pair<>(actor, obj);
		final Event event = () -> repaired.add(entry);
		final long duration = this.duration * cost;
		QUEUE.add(event, duration);

		// Generate discard event
		final Event discardEvent = () -> repaired.stream().filter(e -> e == entry).findFirst().ifPresent(repaired::remove);
		QUEUE.add(discardEvent, discard + duration);

		return duration;
	}
	
	/**
	 * Returns repaired objects belonging to the given actor.
	 * @param actor Actor
	 * @return Repaired objects
	 * TODO - how to make this automatic when entity enters a location?
	 */
	public Stream<WorldObject> getRepaired(Actor actor) {
		final List<Pair<Actor, WorldObject>> results = repaired.stream().filter(entry -> entry.getLeft() == actor).collect(toList());
		repaired.removeAll(results);
		return results.stream().map(Pair::getRight);
	}

	/**
	 * Resets this shop to the initial stock.
	 */
	public void reset() {
		stock.values().stream().forEach(StockLevel::reset);
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
