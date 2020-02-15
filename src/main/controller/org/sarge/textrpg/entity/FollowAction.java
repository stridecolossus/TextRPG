package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.parser.EnumArgumentParser;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.FollowHelper;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Route;
import org.springframework.stereotype.Component;

/**
 * Action to follow a route or entity.
 * @author Sarge
 */
@Component
public class FollowAction extends AbstractAction {
	private static final ArgumentParser.Registry PARSERS = ArgumentParser.Registry.of(Route.class, new EnumArgumentParser<>("route", routes()));

	private static List<Route> routes() {
		return Arrays.stream(Route.values()).filter(Route::isFollowRoute).collect(toList());
	}

	private final FollowHelper helper;

	/**
	 * Constructor.
	 * @param mover Movement controller
	 */
	public FollowAction(FollowHelper helper) {
		super(Flag.INDUCTION);
		this.helper = notNull(helper);
	}

	@Override
	public boolean isValid(Stance stance) {
		return stance != Stance.RESTING;
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		return PARSERS;
	}

	/**
	 * Follows the given route.
	 * @param actor		Actor
	 * @param route		Route to follow
	 * @return Response
	 * @throws ActionException if there not exactly <b>one</b> exit with the given route
	 */
	@RequiresActor
	public Response follow(Entity actor, Route route) throws ActionException {
		final List<Exit> exits = actor.location().exits().stream().filter(exit -> exit.isPerceivedBy(actor)).collect(toList());
		if(exits.isEmpty()) throw ActionException.of("follow.route.none");
		if(exits.size() != 1) throw ActionException.of("follow.route.ambiguous");
		return follow(actor, exits.get(0), route);
	}

	/**
	 * Follows the route in the given direction.
	 * @param actor		Actor
	 * @param dir		Direction
	 * @return Response
	 * @throws ActionException if there is no exit in the given direction or it has no route
	 */
	public Response follow(Entity actor, Direction dir) throws ActionException {
		final Location loc = actor.location();
		final Exit exit = loc.exits().find(dir).orElseThrow(() -> ActionException.of("follow.invalid.direction"));
		final Route route = exit.link().route();
		if(route == Route.NONE) throw ActionException.of("follow.route.none");
		return follow(actor, exit, route);
	}

	/**
	 * Starts following the given route.
	 * @param actor		Actor
	 * @param start		Starting exit
	 * @param route		Route to follow
	 * @return Response
	 */
	private Response follow(Entity actor, Exit start, Route route) {
		final Function<Location, Exit> mapper = loc -> {
			// Enumerate available exits
			final var exits = loc.exits().stream()
				.filter(e -> e.link().route() == route)
				.filter(e -> e.isPerceivedBy(actor))
				.collect(toList());

			// Select exact matching exit
			if(exits.size() == 1) {
				return exits.get(0);
			}
			else {
				return null;
			}
		};

		return helper.follow(actor, start, mapper, "route");
	}

	/**
	 * Follows the given entity.
	 * @param actor		Actor
	 * @param leader	Entity to follow
	 * @return Response
	 * @throws ActionException if the entity cannot be followed
	 */
	@RequiresActor
	public Response follow(CharacterEntity actor, CharacterEntity leader) throws ActionException {
		// Check player allow followers
		if(actor.isPlayer() && leader.isPlayer()) {
			final PlayerCharacter player = (PlayerCharacter) leader;
			if(!player.settings().toBoolean(PlayerSettings.Setting.ALLOW_FOLLOW)) {
				throw ActionException.of("follow.player.disallowed");
			}
		}

		// Start following
		actor.follower().follow(leader);
		return AbstractAction.response("action.follow.entity", leader.name());
	}

	/**
	 * Stops following.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor is not following anyone
	 */
	@RequiresActor
	public Response follow(CharacterEntity actor) throws ActionException {
		final Entity prev = actor.follower().stop();
		return AbstractAction.response("action.stop.follow", prev.name());
	}
}
