package org.sarge.textrpg.runner;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.ElementLoader;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.CalculationLoader;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.EntityValueCalculator;
import org.sarge.textrpg.entity.StarterArea;
import org.sarge.textrpg.entity.StarterAreaLoader;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.Clock;
import org.sarge.textrpg.util.DataSource;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.PeriodModel;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.TimePeriod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application components.
 * @author Sarge
 */
@Configuration
public class ApplicationComponents {
	@Bean(Clock.START_TIME)
	@ConditionalOnProperty(Clock.START_TIME)
	public long clockStartTime(@Value("${clock.start.time}") String start) {
		return LocalDate.parse(start).atTime(LocalTime.NOON).atZone(Clock.ZONE).toEpochSecond() * Duration.ofSeconds(1).toMillis();
	}

	@Bean
	public Event.Queue globalQueue(Event.Queue.Manager manager) {
		return manager.queue("global");
	}

	@Bean
	public DataSource dataSource(@Value("${data.source}") Path root) {
		return new DataSource(root);
	}

	@Bean("period.transition.broadcaster")
	private static PeriodModel.Listener<TimePeriod> broadcaster(SessionManager manager) {
		return period -> {
			final Description description = Description.of(TextHelper.join("notification.period", period.name()));
			manager.players().forEach(p -> p.alert(description));
		};
	}

	@Bean
	public PeriodModel<TimePeriod> timeCycle(DataSource src, PeriodModel.Factory factory, List<PeriodModel.Listener<TimePeriod>> listeners) throws IOException {
		final List<TimePeriod> periods = TimePeriod.load(src.open("data/time.cycle.txt"));
		final PeriodModel<TimePeriod> cycle = factory.create(periods);
		listeners.forEach(cycle::add);
		return cycle;
	}

	@Bean
	public Set<StarterArea> load(DataSource src, StarterAreaLoader loader) throws IOException {
		return loader.load(src.open("data/starter.areas.txt"));
	}

	// TODO - this is a lot of code that probably should be factored out for unit-testing

	@Bean("calc.init")
	public EntityValueCalculator initialiser(DataSource src) throws IOException {
		// Load initialiser XML
		final Element xml = new ElementLoader().load(src.open("data/calc.init.xml"));

		// Create entry loader
		final Function<Element, Calculation> loader = e -> {
			final Attribute attr = e.attribute("attribute").toValue(Attribute.CONVERTER);
			final Calculation calc = actor -> actor.modifier(attr).get();
			final float mod = e.attribute("mod").toFloat();
			return Calculation.scaled(calc, mod);
		};

		// Load entries for each entity-value
		final var builder = new EntityValueCalculator.Builder();
		for(EntityValue key : EntityValue.PRIMARY_VALUES) {
			final var values = xml.child(key.name().toLowerCase()).children("entry").map(loader).collect(toList());
			final Calculation value = Calculation.compound(values, Calculation.Operator.SUM);
			builder.add(key, value);
		}

		// Create initialiser calculator
		return builder.build();
	}

	@Bean("calc.update")
	public EntityValueCalculator updater(DataSource src) throws IOException {
		// Load XML
		final Element xml = new ElementLoader().load(src.open("data/calc.update.xml"));

		// Load calculator for each entity-value
		final CalculationLoader loader = new CalculationLoader();
		final var builder = new EntityValueCalculator.Builder();
		for(EntityValue key : EntityValue.PRIMARY_VALUES) {
			final Element child = xml.child(key.name().toLowerCase());
			final Calculation calc = loader.load(child);
			builder.add(key, calc);
		}

		// Build update calculator
		return builder.build();
	}
}
