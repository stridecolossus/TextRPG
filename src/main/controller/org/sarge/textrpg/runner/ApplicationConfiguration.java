package org.sarge.textrpg.runner;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Coin;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ConverterAdapter;
import org.sarge.textrpg.util.DataSource;
import org.sarge.textrpg.util.DataTable;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application and actions configuration.
 * @author Sarge
 */
@Configuration
public class ApplicationConfiguration {
	private final DataSource folder;

	/**
	 * Constructor.
	 * @param src Data-source
	 */
	public ApplicationConfiguration(DataSource src) {
		this.folder = src.folder("tables");
	}

	@Bean
	public List<Coin> coins(@Value("${data.coins.silver}") int silver, @Value("${data.coins.gold}") int gold) {
		final Coin c = new Coin("coin.copper");
		final Coin s = new Coin("coin.silver", silver, c);
		final Coin g = new Coin("coin.gold", gold, s);
		return List.of(c, s, g);
	}

	@Bean
	public DataTable<Terrain> terrain() throws IOException {
		return DataTable.load(Terrain.class, folder.open("terrain.table.txt"), true);
	}

	@Bean
	public DataTable<Route> route() throws IOException {
		return DataTable.load(Route.class, folder.open("route.table.txt"), true);
	}

	@Bean
	public DataTable<AbstractAction.Effort> effort() throws IOException {
		return DataTable.load(AbstractAction.Effort.class, folder.open("effort.modifier.table.txt"), true);
	}

	// TODO - consolidate following into helper?

	@Bean("converter.difficulty")
	public Converter<Percentile> difficultyConverter() throws IOException {
		final Map<String, Percentile> table = new StrictMap<>();
		final Consumer<String[]> mapper = array -> table.put(array[0], Percentile.CONVERTER.apply(array[1]));
		DataTable.load(folder.open("difficulty.table.txt")).forEach(mapper);
		return new ConverterAdapter<>(table, Percentile.CONVERTER);
	}

	public Converter<Integer> durabilityConverter() throws IOException {
		final Map<String, Integer> table = new StrictMap<>();
		final Consumer<String[]> mapper = array -> table.put(array[0], Converter.INTEGER.apply(array[1]));
		DataTable.load(folder.open("difficulty.table.txt")).forEach(mapper);
		return new ConverterAdapter<>(table, Converter.INTEGER);
	}

	@Bean("converter.intensity")
	public Converter<Percentile> intensityConverter() throws IOException {
		final Map<String, Percentile> table = new StrictMap<>();
		final Consumer<String[]> mapper = array -> table.put(array[0], Percentile.CONVERTER.apply(array[1]));
		DataTable.load(folder.open("intensity.table.txt")).forEach(mapper);
		return new ConverterAdapter<>(table, Percentile.CONVERTER);
	}

	@Bean
	public Function<Stance, Float> stance() throws IOException {
		final DataTable<Stance> table = DataTable.load(Stance.class, folder.open("stance.table.txt"), false);
		final var map = table.column("modifier", Converter.FLOAT);
		return map::get;
	}
}
