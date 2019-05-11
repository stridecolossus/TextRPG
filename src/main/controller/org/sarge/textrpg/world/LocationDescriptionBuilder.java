package org.sarge.textrpg.world;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.stereotype.Component;

/**
 * Builder for the description of a location.
 * @author Sarge
 */
@Component
public class LocationDescriptionBuilder {
	private static final Description EXITS_EMPTY = Description.of("location.description.exits.none");

	private final SnowModel snow;
	private final NameStore store;
	private final ArgumentFormatter.Registry formatters;

	/**
	 * Constructor.
	 * @param snow				Snow model
	 * @param store				Default name-store for directions
	 * @param formatters		Argument formatters
	 */
	public LocationDescriptionBuilder(SnowModel snow, NameStore store, ArgumentFormatter.Registry formatters) {
		this.snow = notNull(snow);
		this.store = notNull(store);
		this.formatters = notNull(formatters);
	}

	/**
	 * Builds the description for the actors current location.
	 * @param actor Actor
	 * @param brief Whether to render brief locations
	 * @return Location description
	 */
	public List<Description> build(Entity actor, boolean brief) {
		// Build header
		final List<Description> descriptions = new ArrayList<>();
		final Location loc = actor.location();
		final Description header = new Description.Builder("location.description.header")
			.name(loc.name())
			.add("area", loc.area().name())
			.build();
		descriptions.add(header);

		// Build location description
		if(!brief && !loc.isProperty(Property.NOT_DESCRIBED)) {
			final Description text = new Description(TextHelper.join(loc.name(), "description"));
			descriptions.add(text);
		}

		// Add contents
		loc.contents().stream()
			.filter(Thing::isAlive)
			.filter(StreamUtil.not(Thing::isQuiet))
			.filter(actor::perceives)
			.map(t -> t.describe(formatters))
			.forEach(descriptions::add);

		// Add snow level description
		if(loc.terrain().isSnowSurface()) {
			final int level = snow.snow(loc);
			if(level > 0) {
				final Percentile p = Percentile.of(level);
				final ArgumentFormatter banding = formatters.get("snow.level");
				final Description description = new Description.Builder("location.snow").add("level", p, banding).build();
				descriptions.add(description);
			}
		}

		// Enumerate visible exits
		final var exits = loc.exits().stream()
			.filter(exit -> !exit.link().isQuiet())
			.filter(exit -> exit.isPerceivedBy(actor))
			.collect(toList());

		// Add exits summary
		if(exits.isEmpty()) {
			descriptions.add(EXITS_EMPTY);
		}
		else {
			final String line = exits.stream().map(this::describe).collect(joining(" "));
			final Description description = new Description.Builder("location.description.exits").add("exits", line, ArgumentFormatter.PLAIN).build();
			descriptions.add(description);
		}

		return descriptions;
	}

	/**
	 * Helper - Describes the given exit.
	 * @param exit Exit
	 * @return Exit description
	 */
	private String describe(Exit exit) {
		// Lookup the direction text
		final String dir = store.get(TextHelper.prefix(exit.direction()));

		// Wrap with link token (if any)
		final Link link = exit.link();
		final String wrapped = link.wrap(dir);

		// Wrap with route icon
		return link.route().wrap(wrapped);
	}
}
