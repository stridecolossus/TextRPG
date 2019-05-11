package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.Coin;
import org.sarge.textrpg.util.WordCursor;
import org.springframework.stereotype.Component;

/**
 * A <i>money</i> argument builder parses a {@link Money} instance from a command.
 * <p>
 * The command words are formatted as a <i>number</i> followed by a <i>coin</i> denomination. e.g.
 * <p>
 * <tt>12 silver</tt>
 * <br><tt>twelve gold</tt></i>
 * <p>
 * @author Sarge
 */
// TODO - optional "pennies" literal suffix
@Component
public class MoneyArgumentParser implements ArgumentParser<Money> {
	private final ArgumentParser<Integer> numParser;
	private final List<Coin> coins;

	/**
	 * Constructor.
	 * @param num		Numeric parser
	 * @param coins		Coin denominations
	 */
	public MoneyArgumentParser(ArgumentParser<Integer> num, List<Coin> coins) {
		this.numParser = notNull(num);
		this.coins = List.copyOf(coins);
	}

	// TODO
	@Override
	public Money parse(WordCursor cursor) {
		final Integer num = numParser.parse(cursor);
		if(num == null) return null;

		final Coin coin = coins.stream().filter(c -> cursor.matches(c.name())).findAny().orElse(null);
		if(coin == null) return null;

		final int value = num * coin.value();
		return new Money(value);
	}

	@Override
	public int count() {
		return 2;
	}
}
