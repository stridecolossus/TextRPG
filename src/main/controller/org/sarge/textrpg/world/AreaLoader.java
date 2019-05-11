package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.View;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.LootFactoryLoader;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.NameStore;
import org.springframework.stereotype.Service;

/**
 * Loader for an area descriptor.
 * @author Sarge
 */
@Service
public class AreaLoader {
	private static final Converter<Weather.Component> WEATHER = Converter.enumeration(Weather.Component.class);

	private final LootFactoryLoader loot;
	private final WeatherController weather;

	/**
	 * Constructor.
	 * @param loot 			Loot-factory loader for area resources
	 * @param weather		Weather controller
	 */
	public AreaLoader(LootFactoryLoader loot, WeatherController weather) {
		this.loot = notNull(loot);
		this.weather = notNull(weather);
	}

	/**
	 * Loads an area descriptor.
	 * @param xml			XML
	 * @param parent		Parent area
	 * @param store			Name-store
	 * @return Area
	 */
	public Area load(Element xml, Area parent, NameStore store) {
		// Start area
		final var builder = new Area.Builder(xml.attribute("name").toText());
		builder.parent(parent);
		builder.store(store);

		// Load default location properties
		xml.children("property").map(Element::text).map(Property.CONVERTER).forEach(builder::property);

		// Load resources
		xml.children("resource").forEach(e -> resource(e, builder));

		// Load ambient events
		xml.find("ambient").map(AreaLoader::ambient).ifPresent(builder::ambient);

		// Load views
		xml.children("view").forEach(e -> loadView(e, builder));

		// Loader weather descriptor
		final var none = xml.attribute("weather").optional();
		if(none.isPresent()) {
			if(!none.get().equals("none")) throw xml.exception("Weather attribute can only be NONE");
			builder.weather(Weather.NONE);
		}
		else {
			final var model = xml.find("weather").map(AreaLoader::weather);
			model.ifPresent(w -> {
				builder.weather(w);
				weather.add(w);
			});
		}

		// Create area
		return builder.build();
	}

	/**
	 * Loads an area resource.
	 */
	private void resource(Element xml, Area.Builder builder) {
		final String res = xml.attribute("res").toText("res");
		final LootFactory factory = loot.load(xml.child());
		builder.resource(res, factory);
	}

	/**
	 * Loads an ambient event.
	 */
	private static AmbientEvent ambient(Element xml) {
		final String name = xml.attribute("name").toText();
		final Duration period = xml.attribute("period").toValue(DurationConverter.CONVERTER);
		final boolean repeat = xml.attribute("repeat").toBoolean(false);
		return new AmbientEvent(name, period, repeat);
	}

	/**
	 * Loads a weather model.
	 */
	private static Weather weather(Element xml) {
		final Weather.Builder builder = new Weather.Builder();
		xml.children().forEach(e -> load(e, builder));
		return builder.build();
	}

	/**
	 * Loads weather min/max.
	 */
	private static void load(Element xml, Weather.Builder builder) {
		// Load weather component
		final Weather.Component type = xml.attribute("type").toValue(WEATHER);
		final int level = xml.attribute("level").toInteger();

		// Load min/max
		switch(xml.name()) {
		case "min":
			builder.min(type, level);
			break;

		case "max":
			builder.min(type, level);
			break;

		default:
			throw xml.exception("Expected min or max weather component");
		}
	}

	/**
	 * Loads a view.
	 */
	private static void loadView(Element xml, Area.Builder builder) {
		final Direction dir = xml.attribute("dir").toValue(Direction.CONVERTER);
		final View view = View.load(xml);
		builder.view(dir, view);
	}
}
