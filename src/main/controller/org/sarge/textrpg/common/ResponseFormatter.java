package org.sarge.textrpg.common;

import static java.util.stream.Collectors.joining;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.StreamUtil;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.DescriptionFormatter;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.world.LightLevelProvider;
import org.sarge.textrpg.world.LocationDescriptionBuilder;
import org.springframework.stereotype.Component;

/**
 * Formatter for a {@link Response}.
 * @author Sarge
 */
@Component
public class ResponseFormatter {
	private static final Description DARK = Description.of("location.description.dark");

	private final DescriptionFormatter formatter;
	private final LightLevelProvider light;
	private final LocationDescriptionBuilder builder;

	/**
	 * Constructor.
	 * @param formatter 	Formatter
	 * @param light			Light-level
	 * @param builder		Location description builder
	 */
	public ResponseFormatter(DescriptionFormatter formatter, LightLevelProvider light, LocationDescriptionBuilder builder) {
		this.formatter = notNull(formatter);
		this.light = notNull(light);
		this.builder = notNull(builder);
	}

	/**
	 * @return Underlying description formatter
	 */
	public DescriptionFormatter formatter() {
		return formatter;
	}

	/**
	 * Formats the given response.
	 * @param actor			Actor
	 * @param store			Name-store
	 * @param response		Response to format
	 * @return Formatted response
	 */
	public String format(PlayerCharacter actor, NameStore store, Response response) {
		// Check for default responses
		if(response == Response.EMPTY) throw new IllegalArgumentException("Cannot format empty response");
		if(response == Response.OK) return "ok";

		// Add responses
		final List<Stream<Description>> list = new ArrayList<>();
		list.add(response.responses());

		// Add location description
		if(response.isDisplayLocation()) {
			list.add(describe(actor, store));
		}

		// Format responses and collate to text
		return list
			.stream()
			.flatMap(s -> s)
			.map(desc -> formatter.format(desc, store))
			.filter(StreamUtil.not(String::isEmpty))
			.collect(joining(Util.CARRIAGE_RETURN))
			.toString();
	}

	/**
	 * Describes the actors location.
	 * @param actor Actor
	 * @param store Name-store
	 * @return Location description
	 */
	private Stream<Description> describe(PlayerCharacter actor, NameStore store) {
		if(light.isAvailable(actor.location())) {
			final var descriptions = builder.build(actor, actor.settings().toBoolean(PlayerSettings.Setting.BRIEF));
			return descriptions.stream();
		}
		else {
			return Stream.of(DARK);
		}
	}
}
