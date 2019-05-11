package org.sarge.textrpg.common;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Formatter for an amount of money.
 */
@Component
public class MoneyFormatter {
	private final List<Coin> coins;
	private final ArgumentFormatter numeric;

	/**
	 * Constructor.
	 * @param coins 		Coins
	 * @param numeric		Numeric formatter
	 */
	public MoneyFormatter(List<Coin> coins, @Qualifier(ArgumentFormatter.NUMERIC) ArgumentFormatter numeric) {
		Check.notEmpty(coins);
		this.coins = coins.stream().sorted(Comparator.comparing(Coin::value).reversed()).collect(toList());
		this.numeric = notNull(numeric);
	}

	/**
	 * Formats the given amount.
	 * @param amount Amount
	 * @return Formatted amount
	 */
	public String format(int amount, NameStore store) {
		// Short-cut test for zero amount
		if(amount == 0) {
			return StringUtils.EMPTY;
		}

		// Build coin denominations
		Check.oneOrMore(amount);
		final List<String> list = new ArrayList<>();
		int remaining = amount;
		for(Coin coin : coins) {
			final int num = remaining / coin.value();
			if(num > 0) {
				// Add number of coins
				final StringBuilder sb = new StringBuilder();
				sb.append(numeric.format(num, null));

				// Add coin name
				final String suffix;
				if(num == 1) {
					suffix = "single";
				}
				else {
					suffix = "multiple";
				}
				sb.append(" ");
				sb.append(store.get(TextHelper.join(coin.name(), suffix)));

				// Add coin entry
				list.add(sb.toString());
			}
			remaining -= num * coin.value();
		}

		// Insert delimiters
		switch(list.size()) {
		case 1:
			// No delimiters
			return list.get(0);

		case 2:
			// Simple AND clause
			return and(list);

		default:
			// Comma-delimited except for last two parts
			final List<String> result = list.subList(0, list.size() - 2);
			result.add(and(list));
			return result.stream().collect(joining(", "));
		}
	}

	/**
	 * Joins the last two entries with an <i>and</i> clause.
	 */
	private static String and(List<String> list) {
		final StringBuilder sb = new StringBuilder();
		final int size = list.size();
		sb.append(list.get(size - 2));
		sb.append(" and ");
		sb.append(list.get(size - 1));
		return sb.toString();
	}
}
