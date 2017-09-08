package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictSet;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectFilter;
import org.sarge.textrpg.object.WorldObject;

/**
 * Shop model.
 * @author Sarge
 */
public class Shop {
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
		public ObjectDescriptor descriptor() {
			return descriptor;
		}

		/**
		 * @return Shop index
		 */
		public int index() {
			return idx;
		}

		/**
		 * @return Number in stock
		 */
		public int count() {
			return count;
		}
	}

	/**
	 * Stock level.
	 */
	private class StockLevel {
		private final int initial;
		private int current;

		private StockLevel(int num) {
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
		return new Topic(TOPIC_NAME, text) {
			@Override
			public Shop shop() {
				return Shop.this;
			}
		};
	}

	private final Set<String> accepts;
	private final Map<ObjectDescriptor, StockLevel> stock;
	private final List<ObjectDescriptor> index;
	private final Optional<RepairShop> repair;

	private boolean open;

	/**
	 * Constructor.
	 * @param accepts	   Object categories that this shop will purchase
	 * @param stock		   Initial stock
	 * @param repair       Optional repair shop
	 */
	public Shop(Set<String> accepts, Map<ObjectDescriptor, Integer> stock, RepairShop repair) {
		this.accepts = new StrictSet<>(accepts);
		this.stock = stock.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> new StockLevel(e.getValue())));
		this.index = new ArrayList<>(stock.keySet());
		this.repair = Optional.ofNullable(repair);
	}

	/**
	 * @return Repair facility at this shop
	 */
	public Optional<RepairShop> repairShop() {
        return repair;
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
		return stock.keySet().stream().filter(filter).map(StockEntry::new).filter(e -> e.count > 0);
	}

	/**
	 * Retrieves an object descriptor by index.
	 * @param idx Index
	 * @return Object descriptor
	 */
	public Optional<ObjectDescriptor> descriptor(int idx) {
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
		final ObjectDescriptor descriptor = obj.descriptor();
		accepts(descriptor, "sell");

		// Check not damaged
		if(obj.isDamaged()) throw new ActionException("sell.damaged.object");

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
	 * Repairs a damaged object.
	 * @param obj		Damaged object
	 * @param actor		Actor
	 * @return Repair duration (ms)
	 * @throws ActionException
	 */
	public long repair(Actor actor, WorldObject obj) throws ActionException {
		checkOpen();
		accepts(obj.descriptor(), "repair");
		if(!repair.isPresent()) new ActionException("repair.cannot.repair");
		return repair.get().repair(actor, obj);
	}

	/**
	 * Resets this shop to the initial stock.
	 */
	public void reset() {
		stock.values().stream().forEach(StockLevel::reset);
	}

	@Override
	public String toString() {
	    // TODO
		return super.toString();
	}
}
