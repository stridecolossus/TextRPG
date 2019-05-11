package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toMap;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;

/**
 * Shop that sells equipment and other objects.
 * @author Sarge
 */
public class Shop {
	/**
	 * Shop stock entry.
	 */
	private class Entry extends AbstractObject {
		private final int idx;
		private final int initial;

		private int current;

		/**
		 * Constructor.
		 * @param num Initial/current stock level
		 */
		private Entry(int index, int num) {
			this.idx = oneOrMore(index);
			this.initial = oneOrMore(num);
			this.current = num;
		}

		/**
		 * Resets this stock entry to its initial level.
		 */
		private void reset() {
			assert initial > 0;
			current = initial;
		}
	}

	/**
	 * Stock descriptor.
	 */
	public static class Stock extends AbstractEqualsObject {
		/**
		 * Creates a descriptor from the given stock entry.
		 */
		private static Stock of(Map.Entry<ObjectDescriptor, Entry> e) {
			final Entry entry = e.getValue();
			return new Stock(entry.idx, e.getKey(), entry.current);
		}

		private final int index;
		private final ObjectDescriptor descriptor;
		private final int num;

		/**
		 * Constructor.
		 * @param index				Index
		 * @param descriptor		Object descriptor
		 * @param num				Available stock
		 * @throws IllegalArgumentException if the value of the given object is zero
		 */
		public Stock(int index, ObjectDescriptor descriptor, int num) {
			if(descriptor.properties().value() < 1) throw new IllegalArgumentException("Stock must have a value: " + descriptor);
			this.index = oneOrMore(index);
			this.descriptor = notNull(descriptor);
			this.num = oneOrMore(num);
		}

		/**
		 * @return Stock entry index
		 */
		public int index() {
			return index;
		}

		/**
		 * @return Object descriptor
		 */
		public ObjectDescriptor descriptor() {
			return descriptor;
		}

		/**
		 * @return Available stock
		 */
		public int number() {
			return num;
		}

		/**
		 * Describes this stock entry.
		 * @param money Money formatter
		 * @return Stock description
		 */
		public Description describe(ArgumentFormatter money) {
			return new Description.Builder("shop.stock")
				.add("index", index)
				.add("name", descriptor.name())
				.add("number", num)
				.add("price", descriptor.properties().value(), money)
				.build();
		}
	}

	private final Map<ObjectDescriptor, Entry> stock;
	private final Map<Integer, ObjectDescriptor> index;
	private final Set<String> cats;
	private final Optional<RepairShop> repair;

	private boolean open;

	/**
	 * Constructor.
	 * @param stock 		Initial stock
	 * @param cats			Object categories that this shop trades
	 * @param repair		Optional repair facility
	 */
	public Shop(List<Stock> stock, Set<String> cats, RepairShop repair) {
		this.stock = stock.stream().collect(toMap(Stock::descriptor, entry -> new Entry(entry.index, entry.num)));
		this.index = stock.stream().collect(toMap(entry -> entry.index, Stock::descriptor));
		this.cats = Set.copyOf(cats);
		this.repair = Optional.ofNullable(repair);
	}

	/**
	 * @return Whether this shop is open
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Sets whether this shop is open.
	 * @param open Open or closed
	 */
	void setOpen(boolean open) {
		this.open = open;
	}

	/**
	 * @return Repair facility
	 */
	public Optional<RepairShop> repair() {
		return repair;
	}

	/**
	 * Looks up a stock entry by index.
	 * @param idx Index
	 * @return Stock entry
	 */
	public Optional<Stock> stock(int idx) {
		final ObjectDescriptor descriptor = index.get(idx);
		if(descriptor == null) {
			return Optional.empty();
		}
		else {
			final Entry entry = stock.get(descriptor);
			if(entry.current == 0) {
				return Optional.empty();
			}
			else {
				final Stock stock = new Stock(idx, descriptor, entry.current);
				return Optional.of(stock);
			}
		}
	}

	/**
	 * Lists available stock matching the given filter.
	 * @param filter Descriptor filter
	 * @return Stock
	 */
	public Stream<Stock> list(ObjectDescriptor.Filter filter) {
		// TODO - cache candidate if stock has not changed?
		return stock.entrySet().stream()
			.filter(entry -> entry.getValue().current > 0)
			.filter(entry -> filter.test(entry.getKey()))
			.map(Stock::of);
	}

	/**
	 * Sells the given object to the shop.
	 * @param obj Object to sell
	 * @throws ActionException if this shop will not buy the given type of object or it is damaged
	 */
	public void sell(WorldObject obj) throws ActionException {
		// Check shop trades the given object
		final var descriptor = obj.descriptor();
		if(!descriptor.characteristics().categories().stream().anyMatch(cats::contains)) {
			throw ActionException.of("shop.buy.invalid");
		}

		// Check not damaged
		if(obj.isDamaged()) throw ActionException.of("shop.buy.damaged");

		// Add to stock
		final Entry entry = stock.get(descriptor);
		if(entry != null) {
			++entry.current;
		}
		obj.destroy();
	}

	/**
	 * Buys objects of the given type from this shop.
	 * @param descriptor	Object descriptor
	 * @param num			Number to buy
	 * @throws ActionException if this shop does not sell the given type of object or has insufficient stock
	 */
	public void buy(ObjectDescriptor descriptor, int num) throws ActionException {
		Check.notNull(descriptor);
		final Entry entry = stock.get(descriptor);
		if(entry.current == 0) throw ActionException.of("shop.stock.empty");
		if(entry.current < num) throw ActionException.of("shop.stock.insufficient");
		entry.current -= num;
	}

	/**
	 * Resets this shop to its initial stock.
	 */
	public void reset() {
		stock.entrySet().removeIf(entry -> entry.getValue().initial == 0);
		stock.values().forEach(Entry::reset);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("open", open).build();
	}
}
