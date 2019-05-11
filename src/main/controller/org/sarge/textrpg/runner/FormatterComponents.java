package org.sarge.textrpg.runner;

import org.sarge.textrpg.common.MoneyFormatter;
import org.sarge.textrpg.common.NumericConverter;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.BandingTable;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description formatting components.
 * @author Sarge
 */
@Configuration
public class FormatterComponents {
	/**
	 * Creates the numeric argument formatter.
	 * @param converter Numeric converter
	 * @return Numeric argument formatter
	 */
	@Bean(ArgumentFormatter.NUMERIC)
	public ArgumentFormatter numeric(NumericConverter converter) {
		return ArgumentFormatter.integral(converter::convert);
	}

	/**
	 * Creates the money argument formatter.
	 * @param formatter Money formatter
	 * @return Money argument formatter
	 */
	@Bean(ArgumentFormatter.MONEY)
	public ArgumentFormatter money(MoneyFormatter formatter) {
		return (arg, store) -> formatter.format((Integer) arg, store);
	}

	/**
	 * Creates a registry of <b>all</b> argument formatters.
	 * @param banding		Banding tables
	 * @param numeric		Numeric formatter
	 * @param money			Money formatter
	 * @return Formatter registry
	 */
	@Bean
	public ArgumentFormatter.Registry formatters(BandingTableConfiguration banding, @Qualifier(ArgumentFormatter.NUMERIC) ArgumentFormatter numeric, @Qualifier(ArgumentFormatter.MONEY) ArgumentFormatter money) {
		// Create registry
		final ArgumentFormatter.Registry formatters = new ArgumentFormatter.Registry();

		// Add banding tables
		banding.tables().entrySet().forEach(entry -> formatters.add(entry.getKey(), formatter(entry.getValue())));

		// Add default formatters
		formatters.add(ArgumentFormatter.NUMERIC, numeric);
		formatters.add(ArgumentFormatter.MONEY, money);

		return formatters;
	}

	/**
	 * Creates an argument formatter for the given banding-table.
	 * @param table Banding-table
	 * @return Argument formatter
	 */
	private static ArgumentFormatter formatter(BandingTable<Percentile> table) {
		return (arg, store) -> store.get(table.map((Percentile) arg));
	}
}
