package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;

/**
 * Shop that trades pets or mounts.
 * @author Sarge
 */
public class PetShop {
	/**
	 * Pet-shop stock entry.
	 */
	public static class Stock {
		private final Race race;
		private final Function<Race, Entity> ctor;
		private final int cost;
		private final int hire;
		private final int initial;
		private int count;

		/**
		 * Constructor.
		 * @param race		Race
		 * @param ctor		Creature constructor
		 * @param count		Initial stock count
		 * @param cost		Buy cost
		 * @param hire		Hire cost
		 */
		public Stock(Race race, Function<Race, Entity> ctor, int count, int cost, int hire) {
			this.race = notNull(race);
			this.ctor = notNull(ctor);
			this.initial = oneOrMore(count);
			this.cost = oneOrMore(cost);
			this.hire = oneOrMore(hire);
			reset();
		}

		/**
		 * Resets this stock entry to its initial level.
		 */
		private void reset() {
			count = initial;
		}

		/**
		 * @return Stock level
		 */
		int count() {
			return count;
		}

		/**
		 * Describes this pet stock entry.
		 * @param money Monetary formatter
		 * @return Description
		 */
		public Description describe(ArgumentFormatter money) {
			return new Description.Builder("pet.shop.stock")
				.name(race.name())
				.add("count", count)
				.add("cost", cost, money)
				.add("hire", hire, money)
				.build();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	private final List<Stock> stock;

	/**
	 * Constructor.
	 * @param stock Initial stock
	 */
	public PetShop(List<Stock> stock) {
		this.stock = List.copyOf(stock);
	}

	/**
	 * @return Stock
	 */
	public Stream<Stock> stock() {
		return stock.stream().filter(entry -> entry.count > 0);
	}

	/**
	 * Buys or hires a creature.
	 * @param race Race to purchase
	 * @return New creature
	 * @throws ActionException if the shop does not stock the given creature
	 */
	public Entity purchase(Race race) throws ActionException {
		// Check stock
		final Stock entry = stock.stream().filter(e -> e.race == race).findAny().orElseThrow(() -> ActionException.of("pet.shop.purchase.invalid"));
		if(entry.count == 0) throw ActionException.of("pet.shop.purchase.empty");

		// Consume stock
		--entry.count;

		// Create creature
		return entry.ctor.apply(entry.race);
	}

	/**
	 * Sells a creature to this shop.
	 * @param creature Creature to sell
	 * @throws ActionException if this shop does not trade in the given creature
	 */
	public void sell(Entity creature) throws ActionException {
		final Stock entry = stock.stream().filter(e -> e.race == creature.descriptor().race()).findAny().orElseThrow(() -> ActionException.of("pet.shop.sell.invalid"));
		++entry.count;
		creature.destroy();
	}

	/**
	 * Resets this pet-shop to its initial stock levels.
	 */
	public void reset() {
		stock.forEach(Stock::reset);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
