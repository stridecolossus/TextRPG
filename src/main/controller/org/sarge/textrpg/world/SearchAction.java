package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.EffortAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.PerceptionCalculator;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.DiscoverController;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to search for hidden objects or entities.
 * @author Sarge
 */
@EffortAction
@Component
public class SearchAction extends AbstractAction {
	private final LightLevelProvider light;
	private final PerceptionCalculator perception;
	private final Event.Queue.Manager manager;

	private Function<Effort, Percentile> modifier = effort -> Percentile.ONE;
	private Duration base = Duration.ofMinutes(1);
	private Duration forget = Duration.ofHours(1);

	/**
	 * Constructor.
	 * @param light			Light-level provider
	 * @param perception	Perception controller
	 * @param manager		Queue manager for search instances
	 */
	public SearchAction(LightLevelProvider light, PerceptionCalculator perception, Event.Queue.Manager manager) {
		super(Flag.LIGHT, Flag.INDUCTION);
		this.light = notNull(light);
		this.perception = notNull(perception);
		this.manager = notNull(manager);
	}

	/**
	 * Sets the base duration for a {@link Effort#NORMAL} search.
	 * @param duration Base duration
	 */
	@Autowired
	public void setDuration(@Value("${search.base.duration}") Duration duration) {
		this.base = DurationConverter.oneOrMore(duration);
	}

	/**
	 * Sets the forget period for discovered objects.
	 * @param forget Forget period
	 */
	@Autowired
	public void setForgetPeriod(@Value("${search.forget.duration}") Duration forget) {
		this.forget = DurationConverter.oneOrMore(forget);
	}

	/**
	 * Sets the effort modifier.
	 * @param modifier Effort modifier
	 */
	@Autowired
	public void setEffortMapper(@Value("#{effort.function('search', T(org.sarge.textrpg.util.Percentile).CONVERTER)}") Function<Effort, Percentile> modifier) {
		this.modifier = notNull(modifier);
	}

	/**
	 * Searches the current location.
	 * @param actor			Actor
	 * @param effort		Effort
	 * @return Response
	 */
	@RequiresActor
	public Response search(PlayerCharacter actor, Effort effort) {
		// Calculate effort scale
		final Percentile scale = modifier.apply(effort);

		// Create perception filter
		final Percentile score = perception.score(actor);
		final Percentile level = light.level(actor.location());
		final Percentile total = score.scale(level).scale(scale);
		final var filter = perception.filter(total, Thing::visibility);
		// TODO - weather

		// Enumerate hidden objects
		final Location loc = actor.location();
		final Stream<? extends Thing> stream = loc.contents().stream().filter(StreamUtil.not(actor::perceives)).filter(filter);

		// Create discovery callback
		final Consumer<Thing> listener = obj -> {
			final Description message = new Description("search.discovered", obj.name());
			actor.alert(message);
			actor.hidden().add(obj, forget);
		};

		// Calculate scaled duration
		final Duration duration = base.multipliedBy(effort.ordinal() + 1);

		// Create search induction
		final DiscoverController<Thing> controller = new DiscoverController<>("search", Thing::visibility, manager.queue("search", true));
		final Induction induction = controller.induction(duration, stream, listener);

		// Build response
		return Response.of(new Induction.Instance(induction, duration));
	}
}
