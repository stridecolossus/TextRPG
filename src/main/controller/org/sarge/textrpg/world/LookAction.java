package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.time.LocalTime;
import java.util.Set;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.View;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Notification;
import org.sarge.textrpg.entity.PerceptionCalculator;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.Window;
import org.sarge.textrpg.parser.LiteralArgumentParser;
import org.sarge.textrpg.util.Clock;
import org.sarge.textrpg.util.Clock.DateTimeClock;
import org.sarge.textrpg.util.Percentile;
import org.springframework.stereotype.Component;

/**
 * Describes the current location or looks in a given direction.
 * @author Sarge
 */
@Component
public class LookAction extends AbstractAction {
	private static final Response EMPTY = Response.of("look.nothing");
	private static final Response DARK = Response.of("look.requires.light");

	/**
	 * Synthetic look <i>around</i> argument.
	 */
	static final class AroundArgument implements CommandArgument {
		@Override
		public String name() {
			return "look.around";
		}
	}

	/**
	 * Command argument for the {@link #look(Entity, AroundArgument)} action.
	 */
	public static final AroundArgument AROUND = new AroundArgument();

	private static final ArgumentParser.Registry PARSERS = ArgumentParser.Registry.of(AroundArgument.class, new LiteralArgumentParser<>(AROUND));

	private final DateTimeClock clock;
	private final LightLevelProvider light;
	private final EmissionController controller;
	private final PerceptionCalculator perception;

	/**
	 * Constructor.
	 * @param clock			Clock
	 * @param light 		Light-level provider
	 * @param controller	Emission broadcaster
	 * @param perception	Passive perception controller
	 */
	public LookAction(Clock clock, LightLevelProvider light, EmissionController controller, PerceptionCalculator perception) {
		super(Flag.OUTSIDE);
		this.clock = DateTimeClock.of(clock);
		this.light = notNull(light);
		this.controller = notNull(controller);
		this.perception = notNull(perception);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	public boolean isValid(Terrain terrain) {
		return true;
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		return PARSERS;
	}

	/**
	 * Describes the current location.
	 * @return Response
	 */
	public Response look() {
		return Response.DISPLAY_LOCATION;
	}

	/**
	 * Describes the view in the given direction.
	 * @param actor		Actor
	 * @param dir		Direction
	 * @return Response
	 */
	@RequiresActor
	public Response look(Entity actor, Direction dir) {
		// Check whether daylight
		if(!light.isDaylight()) return DARK;

		// Check whether can see view from this terrain
		final Location loc = actor.location();
		if(!isOpenTerrain(loc)) return EMPTY;

		// Find view if any
		final View view = actor.location().area().view(dir).orElse(View.NONE);
		if(view == View.NONE) return EMPTY;

		// Describe view
		final LocalTime time = clock.toDateTime().toLocalTime();
		return Response.of(view.describe(time));
	}

	/**
	 * Looks around for emissions in nearby locations.
	 * @param actor			Actor
	 * @param around		Argument (unused)
	 * @return Response
	 */
	@RequiresActor
	public Response look(Entity actor, AroundArgument around) {
		// Scan neighbours for visible emissions
		// TODO - weather, e.g. snow obscures
		final var notifications = controller.find(Set.of(Emission.LIGHT, Emission.SMOKE), actor.location());

		// Describe detected emissions
		final Percentile score = perception.score(actor);
		final var filter = perception.filter(score, EmissionNotification::intensity);
		final var responses = notifications.stream().filter(filter).map(Notification::describe).collect(toList());

		// Build response
		if(responses.isEmpty()) {
			return EMPTY;
		}
		else {
			return Response.of(responses);
		}
	}

	/**
	 * Looks through a window.
	 * @param actor			Actor
	 * @param window		Window
	 * @return Response
	 */
	@RequiresActor
	public Response look(Entity actor, Window window) {
		final LocalTime time = clock.toDateTime().toLocalTime();
		final String view = window.view().describe(time);
		return Response.of(view);
	}

	/**
	 * Looks at an object.
	 * @param thing Object
	 * @return Response
	 */
	public Response look(Thing thing) {
		return Response.of(thing.name());
	}

	/**
	 * @return Whether the given location is open terrain
	 */
	private static boolean isOpenTerrain(Location loc) {
		switch(loc.terrain()) {
		case JUNGLE:
		case FOREST:
		case WOODLAND:
		case INDOORS:
			return false;

		case UNDERGROUND:
			// TODO - how to have views underground, e.g. across bridge of moria, or down chasm? => area has default terrain, view = loc.terrain == area.default.terrain
			return false;

		default:
			return true;
		}
	}
}
