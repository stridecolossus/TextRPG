package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Clock;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.EnvironmentNotification;
import org.sarge.textrpg.common.Event;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.common.MovementNotification;
import org.sarge.textrpg.object.Vehicle;
import org.sarge.textrpg.util.DataTableCalculator;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;
import org.sarge.textrpg.world.Tracks;

/**
 * Entity movement controller.
 * <p>
 * The controller is responsible for:
 * <ul>
 * <li>consuming stamina</li>
 * <li>moving the entity (and any parent container such as a vehicle) to the new location</li>
 * <li>adding tracks</li>
 * <li>generating environmental notifications, e.g. noise</li>
 * <li>auto-moving the associated group if the entity is the leader</li>
 * </ul>
 * @author Sarge
 */
public class MovementController {
	private static final Emission.Type[] EMISSIONS = {Emission.Type.LIGHT, Emission.Type.SMOKE, Emission.Type.SOUND};

	private final Clock clock;
	private final DataTableCalculator move;
	private final DataTableCalculator tracks;
	private final long lifetime;

	/**
	 * Constructor.
	 * @param clock			Game-time clock
	 * @param move			Movement cost calculator
	 * @param tracks		Tracks visibility calculator
	 * @param lifetime		Tracks lifetime
	 */
	public MovementController(Clock clock, DataTableCalculator move, DataTableCalculator tracks, long lifetime) {
		Check.notNull(move);
		Check.notNull(tracks);
		Check.oneOrMore(lifetime);
		this.clock = notNull(clock);
		this.move = move;
		this.tracks = tracks;
		this.lifetime = lifetime;
	}

	/**
	 * Moves an entity in the given direction and generates notifications.
	 * @param actor		Actor
	 * @param dir		Direction to move
	 * @param mod		Cost multiplier
	 * @param player	Whether moving a player or an NPC
	 * @return Move description
	 * @throws ActionException if the actor cannot move in the given direction, is in an invalid state, or has insufficient stamina
	 * @see Entity#move(Link, int)
	 * TODO - several bits should be moved to MoveAction (for player-only stuff)
	 */
	public Description move(Entity actor, Direction dir, float mod, boolean player) throws ActionException {
		if(mod < 1) throw new IllegalArgumentException("Movement cost modifier must be one-or-more");

		// Check can traverse
		final Location loc = actor.getLocation();
		final Exit exit = loc.getExits().get(dir);
		final Location dest = exit.getDestination();
		final Vehicle vehicle = ActionHelper.getVehicle(actor);
		assert exit.perceivedBy(actor);
		if(player) {
			// TODO - move to MoveAction
			checkLink(actor, vehicle, exit);
		}
		
		// Check stamina
		final float cost = calculateMovementCost(actor, exit, vehicle, mod);
		if(actor.getValues().get(EntityValue.STAMINA) < cost) throw new ActionException("move.insufficient.stamina");
		
		// Traverse link
		if(player) {
			// TODO - move to MoveAction
			exit.getLink().getScript().execute(actor);
		}
		if(vehicle == null) {
			actor.setParent(dest);
		}
		else {
			vehicle.setParent(dest);
		}
		actor.modify(EntityValue.STAMINA, (int) -cost);
		
		// Update environment
		addTracks(actor, loc, dir, exit);
		generateNotifications(loc, dest, actor, dir);
		
		// Update ambient events
		if(player) {
			// TODO - move to MoveAction
			generateAmbientEvents(loc, dest, actor);
		}
		
		// Move group members if leader
		moveGroup(actor, dir);
		
		// Describe destination
		// TODO - move to MoveAction
		if(player) {
			return dest.describe(clock.isDaylight(), actor);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Checks that the link can be traversed by the given actor.
	 * @param actor		Actor
	 * @param vehicle	Vehicle
	 * @param exit		Exit
	 * @throws ActionException if the link cannot be traversed
	 * @see Link#getReason()
	 */
	private static void checkLink(Entity actor, Vehicle vehicle, Exit exit) throws ActionException {
		// Check link can be traversed by this actor
		final Link link = exit.getLink();
		if(!link.isTraversable(actor)) {
			final Object arg = link.getController().orElse(null);
			throw new ActionException(link.getReason(), arg);
		}

		// Check terrain
		if(vehicle == null) {
			// Check swimming if moving into a water location
			final boolean here = actor.getLocation().getTerrain() == Terrain.WATER;
			final boolean there = exit.getDestination().getTerrain() == Terrain.WATER;
			if(!here && there && !actor.isSwimming()) {
				throw new ActionException("move.not.swimming");
				// TODO - modify cost by skill level
			}
		}
		else {
			// Otherwise check vehicle can enter this location
			if(!vehicle.getDescriptor().isValid(link.getRoute())) throw new ActionException("vehicle.invalid.route", vehicle);
		}
	}
	
	/**
	 * Calculates movement cost of traversing the link.
	 * @param actor		Actor
	 * @param exit		Exit descriptor
	 * @return Movement cost
	 */
	private float calculateMovementCost(Entity actor, Exit exit, Vehicle vehicle, float mod) {
		final float vehicleMod = vehicle == null ? 1 : vehicle.getDescriptor().getMovementCostModifier();
		final Link link = exit.getLink();
		final Route route = link.getRoute();
		final float cost = move.multiply(exit.getDestination().getTerrain(), route, actor.getStance());
		final int reduce = actor.getValues().get(EntityValue.MOVE_COST);
		return Math.max(1, (cost * mod * vehicleMod) - reduce);
	}

	/**
	 * Adds tracks <b>from</b> the previous location.
	 */
	private void addTracks(Entity actor, Location loc, Direction dir, Exit exit) {
		// TODO - weather
		final float vis = tracks.multiply(exit.getDestination().getTerrain(), exit.getLink().getRoute(), actor.getStance());
		if(vis > 0) {
			final Tracks tracks = new Tracks(actor.getRace().getName(), loc, dir, new Percentile(vis), System.currentTimeMillis()); // TODO - ok? no context here, add a member?
			actor.add(tracks, lifetime);
		}
	}

	/**
	 * Generates notifications.
	 * @param prev		Previous location
	 * @param dest		Destination
	 * @param actor		Actor
	 * @param dir		Direction
	 */
	private static void generateNotifications(Location prev, Location dest, Entity actor, Direction dir) {
		// Generate movement notifications
		// TODO - modify by visibility, weather, day/night, etc
		final boolean player = actor instanceof Player;
		prev.broadcast(actor, new MovementNotification(actor, false, dir, player));
		dest.broadcast(actor, new MovementNotification(actor, true, dir.reverse(), player));
		
		// Generate environmental notifications
		// TODO - modify by visibility, weather, day/night, etc
		// TODO - noisier if fleeing?
		// TODO - only if combat or cart
		for(Emission.Type type : EMISSIONS) {
			actor
				.getEmission(type)
				.map(e -> new EnvironmentNotification(e, dir))
				.ifPresent(n -> prev.broadcast(actor, n));
		}
	}
	
	/**
	 * Generates ambient events.
	 * @param prev		Previous location
	 * @param next		Next location
	 * @param actor		Actor
	 */
	private static void generateAmbientEvents(Location prev, Location next, Entity actor) {
		final Area area = next.getArea();
		if(area != prev.getArea()) {
			area.getAmbientEvents().forEach(e -> create(actor, e, area));
		}
	}
	
	/**
	 * Registers an ambient event.
	 */
	private static void create(Entity actor, Area.Ambient ambient, Area area) {
		// Register event
		final Event event = () -> {
			if(actor.getLocation().getArea() == area) {
				// Display if still in this area
				actor.alert(new Message(ambient.getName()));
				
				// Repeat
				if(ambient.isRepeating()) {
					create(actor, ambient, area);
				}
			}
		};
		actor.getEventQueue().add(event, ambient.getPeriod());
	}

	/**
	 * Moves group and followers.
	 */
	private void moveGroup(Entity actor, Direction dir) {
		// Move group
		// TODO - this seems convoluted? just if..then check for whether actor is leader?
		actor.getGroup()
			.filter(g -> g.getLeader() == actor)
			.map(Group::getMembers)
			.ifPresent(members -> members.forEach(e -> move(e, dir)));

		// Move followers
		actor.getFollowers().forEach(e -> move(e, dir));
	}
	
	/**
	 * Moves other entities.
	 * @param entities		Entity to move
	 * @param dir			Direction
	 */
	private void move(Entity actor, Direction dir) {
		try {
			move(actor, dir, 1, actor instanceof Player);
		}
		catch(ActionException ex) {
			actor.getNotificationHandler().handle(new Message(ex.getMessage()));
		}
	}
}
