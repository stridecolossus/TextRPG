package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.textrpg.common.EffortAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.util.ActionException;
import org.springframework.beans.factory.annotation.Value;

/**
 * Action to discover and follow {@link Trail}.
 * @author Sarge
 */
@RequiresActor
@EffortAction
public class TrackAction extends SkillAction {
	private static final ActionException NONE = ActionException.of("follow.tracks.none");

	private final TracksController controller;
//	private final DiscoverController<Trail> discover;
	private final Duration forget;
	private final FollowHelper helper;

	/**
	 * Constructor.
	 * @param skill			Tracking skill
	 * @param controller	Tracks controller
	 * @param forget		Forget period
	 * @param helper		Follow helper
	 */
	public TrackAction(@Value("#{skills[tracking]}") Skill skill, TracksController controller, @Value("${track.forget}") Duration forget, FollowHelper helper) {
		super(skill, Flag.INDUCTION, Flag.LIGHT);
		this.controller = notNull(controller);
		this.forget = notNull(forget);
//		this.discover = new DiscoverController<>("track", controller::visibility);
		// TODO - tracks pruning
		this.helper = notNull(helper);
	}

	/**
	 * Searches for <b>any</b> tracks in the current location.
	 * @param actor			Actor
	 * @param effort		Effort
	 * @return Response
	 */
	public Response track(PlayerCharacter actor, Effort effort) {
		return track(actor, null, effort);
	}

	/**
	 * Searches for specific tracks.
	 * @param actor			Actor
	 * @param name			Entity name/race to track
	 * @param effort		Effort
	 * @return Response
	 */
	public Response track(PlayerCharacter actor, /* TODO - custom argument type (or Race?) */ String name, Effort effort) {
//		// Enumerate tracks
//		final var filter = filter(actor, name, effort);
//		final var stream = actor.destination().tracks().filter(filter);
//
//		// Calculate duration
//		final Duration duration = super.controller().duration(actor, effort);
//
//		// Create track callback
//		final Consumer<Trail> listener = tracks -> {
//			// Calculate visibility
//			final Percentile vis = controller.visibility(tracks);
//
//			// Determine age key
//			final String age = controller.age(tracks);
//
//			// Build tracks description
//			final Description description = new Description.Builder("notification.tracks.discovered")
//				.add("creator", tracks.creator())
//				.add("dir", tracks.direction().name())
//				.add("tracks.visibility", vis, Description.Format.BANDING)
//				.add("age", age)
//				.build();
//
//			// Notify discovered tracks
//			actor.alert(description);
//
//			// Record tracks
//			actor.hidden().add(tracks, forget);
//		};
//
//		// Build response
//		// TODO - scale visibility by effort
//		final Induction induction = discover.induction(duration, stream, listener);
//		return Response.of(induction, duration);
		return null;
	}

//	/**
//	 * Creates the filter for tracks.
//	 * @param actor			Actor
//	 * @param name			Entity name/race
//	 * @param effort		Effort modifier
//	 * @return Tracks filter
//	 */
//	private Predicate<Trail> filter(Entity actor, String name, Effort effort) {
//		// Filter by skill
//		final Percentile score = super.controller().score(actor);
//		final Percentile scale = controller.modifier(effort);
//		final Predicate<Trail> filter = t -> t.visibility().scale(scale).isLessThan(score);
//
//		// Optionally filter by race
//		if(name == null) {
//			return filter;
//		}
//		else {
//			return filter.and(filter(name));
//		}
//	}
//
//	/**
//	 * Helper - Creates a tracks filter by race.
//	 */
//	private static Predicate<Trail> filter(String name) {
//		return t -> t.creator().equals(name);
//	}
//
//	/**
//	 *
//	 * TODO
//	 * - ensure use direction from tracks
//	 * - follower should iterate tracks
//	 * - display next on move
//	 *
//	 */
//
//	/**
//	 * Follow specific tracks.
//	 * @param actor
//	 * @param skill
//	 * @param race
//	 * @return
//	 * @throws ActionException
//	 */
//	public Response follow(PlayerCharacter actor, String name, Effort effort) throws ActionException {
//		// Find tracks
//		final var tracks = StreamUtil.select(Trail.class, actor.hidden().stream())
//			.filter(filter(name))
//			.collect(toList());
//
//		// Check single result
//		if(tracks.isEmpty()) throw NONE;
//		if(tracks.size() != 1) throw ActionException.of("follow.tracks.ambiguous");
//
//		// Order by visibility
//		if(tracks.size() > 1) {
//			Collections.sort(tracks, Comparator.comparing(Trail::visibility));
//		}
//
//		// Follow most visible tracks
//		final Direction dir = tracks.get(0).direction();
//		return null;
//		//return followLocal(actor, name, effort, dir);
//	}
//
//	/**
//	 * Follow specific tracks in the given direction.
//	 * @param actor
//	 * @param skill
//	 * @param race
//	 * @param dir
//	 * @return
//	 * @throws ActionException
//	 */
//	public Response follow(PlayerCharacter actor, String name, Direction dir, Effort effort) throws ActionException {
////		// Check tracks known
////		final Tracks tracks = StreamUtil.select(Tracks.class, actor.known())
////			.filter(t -> t.direction() == dir)
////			.filter(filter(name))
////			.findAny()
////			.orElseThrow(() -> NONE);
////		// TODO - perceived exit
////
////		// Check exit known
////		final Exit exit = actor.destination().exits().find(dir).get();
//
//		return null;
//	}
//
//	private Response follow(PlayerCharacter actor, Exit start, String name, Effort effort) {
//		final Function<Location, Exit> mapper = loc -> {
//			// Enumerate matching tracks
//			// TODO - rather than searching in location should be using tracks::next (and then check same creator)
//			final var tracks = loc.tracks()
//				.filter(t -> t.creator().equals(name))
//				.sorted()
//				.collect(toList());
//
//			// Stop if none
//			if(tracks.isEmpty()) return null;
//
//			// Lookup exit
//			final Direction dir = tracks.get(0).direction();
//			return loc.exits().find(dir).get();
//		};
//
//		return helper.follow(actor, start, mapper, "track");
//	}
}
