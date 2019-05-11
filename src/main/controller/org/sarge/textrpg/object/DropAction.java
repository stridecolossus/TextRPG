package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Equipment;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.CurrentLink;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Property;
import org.sarge.textrpg.world.RiverController;
import org.springframework.stereotype.Component;

/**
 * Drops an object to the current location.
 * @author Sarge
 */
@Component
@RequiresActor
public class DropAction extends AbstractAction {
	private final ObjectController controller;
	private final RiverController river;

	/**
	 * Constructor.
	 * @param controller		Object controller for dropped objects
	 * @param river				river controller for floating objects dropped into a river current
	 */
	public DropAction(ObjectController controller, RiverController river) {
		super(Flag.OUTSIDE, Flag.BROADCAST);
		this.controller = notNull(controller);
		this.river = notNull(river);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Drops the given object to the current location.
	 * @param actor		Actor
	 * @param obj		Object to drop
	 * @return Response
	 */
	public Response drop(Entity actor, @Carried WorldObject obj) {
		return Response.of(dropLocal(actor, obj));
	}

	/**
	 * Drops objects matching the given filter.
	 * @param actor			Actor
	 * @param filter		Filter
	 * @return Response
	 * @throws ActionException if there are no matching objects
	 */
	public Response drop(Entity actor, ObjectDescriptor.Filter filter) throws ActionException {
		// Enumerate objects to drop
		final Equipment equipment = actor.contents().equipment();
		final List<WorldObject> objects = StreamUtil.select(WorldObject.class, actor.contents().stream())
			.filter(StreamUtil.not(equipment::contains))
			.filter(WorldObject.Filter.of(filter))
			.collect(toList());

		// Check matching objects
		if(objects.isEmpty()) throw ActionException.of("drop.none.matched");

		// Drop objects
		final var responses = objects.stream().map(obj -> dropLocal(actor, obj)).collect(toList());

		// Build response
		return Response.of(responses);
	}

	/**
	 * Recursively finds the bottom location.
	 * @param loc Location
	 * @return Bottom location
	 */
	private static Location find(Location dest) {
		if(dest.isProperty(Property.FLOORLESS)) {
			final Exit exit = dest.exits().find(Direction.DOWN).get();
			return find(exit.destination());
		}
		else {
			return dest;
		}
	}

	/**
	 * Drops an object.
	 * @param actor			Actor
	 * @param object		Object to drop
	 * @return Response
	 */
	private Description dropLocal(Entity actor, WorldObject obj) {
		// Check not equipped
		final Equipment equipment = actor.contents().equipment();
		final Slot slot = equipment.slot(obj);
		if(slot != null) {
			if(slot.isHanded()) {
				equipment.remove(slot);
			}
			else {
				return new Description("drop.object.equipped", obj.name());
			}
		}

		// Determine the bottom of this location
		final Location loc = actor.location();
		final Location bottom = find(loc);

		// Destroy if object is fragile and dropped from height
		if(loc != bottom) {
			obj.damage(Damage.Type.CRUSHING, 1);
			if(!obj.isAlive()) {
				final String noise = TextHelper.join("drop.noise", obj.descriptor().characteristics().material().name());
				final Description description = new Description.Builder("drop.object.smashed").name(obj.name()).add("noise", noise).build();
				bottom.broadcast(null, description);
				return description;
			}
		}

		// Check for object dropped into water
		final boolean water = bottom.isWater();
		if(water) {
			// Determine whether floats
			if(obj.descriptor().characteristics().material().isFloating()) {
				// Register as floating if dropped into river current
				CurrentLink.find(loc).ifPresent(exit -> river.add(obj, exit));
			}
			else {
				// Otherwise destroy
				obj.destroy();
			}

			// Broadcast message
			final Description description = new Description("drop.object.water", obj.name());
			bottom.broadcast(null, description);
			return description;
		}

		// Drop object and register decay event
		obj.parent(bottom);
		controller.decay(obj);

		// Build response
		final String suffix = water ? "water" : "response";
		return new Description(TextHelper.join("drop.object", suffix), obj.name());
	}
}
