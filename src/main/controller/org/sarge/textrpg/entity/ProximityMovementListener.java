package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashSet;
import java.util.Set;

import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.LocationBroadcaster;
import org.sarge.textrpg.world.MovementController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Movement listener that generates proximity notifications for entities with glowing weapons.
 * @author Sarge
 */
@Component
public class ProximityMovementListener implements MovementController.Listener {
	private final Set<Entity> entities = new HashSet<>();

	private final LocationBroadcaster visitor;

	private ArgumentFormatter formatter = ArgumentFormatter.PLAIN;

	/**
	 * Constructor.
	 * @param max Maximum traversal depth
	 */
	public ProximityMovementListener(@Value("${proximity.traversal.depth}") int max) {
		visitor = new LocationBroadcaster(max);
	}

	/**
	 * Sets the glow intensity formatter.
	 * @param formatter Formatter
	 */
	@Autowired
	public void setIntensityFormatter(@Value("#{formatters.get('glow.weapon.intensity')}") ArgumentFormatter formatter) {
		this.formatter = notNull(formatter);
	}

	@Override
	public boolean isPlayerOnly() {
		return true;
	}

	@Override
	public void update(Entity actor, Exit exit, Location prev) {
		if(actor.descriptor().alignment() != Alignment.NEUTRAL) {
			visitor.visit(actor.location(), this::visit);
		}
	}

	/**
	 * Notifies entities in the given exit destination.
	 * @param exit		Exit
	 * @param depth		Depth
	 */
	private void visit(Exit exit, int depth) {
		// TODO - restrict by area? but then what about links over area borders? or maybe ignore if 2 or more areas 'distance'?
		exit.destination().contents()
			.select(Entity.class)
			.filter(entities::contains)
			.forEach(e -> alert(e, depth));
	}

	/**
	 * Alerts the given entity of a nearby enemy.
	 * @param entity		Entity
	 * @param depth			Proximity depth
	 */
	private void alert(Entity entity, int depth) {
		final Description alert = new Description.Builder("proximity.alert").add("intensity", Percentile.of(depth, visitor.max()), formatter).build();
		entity.alert(alert);
	}

	/**
	 * Registers an entity with an equipped glowing weapon.
	 * @param entity Entity to add
	 */
	public void add(Entity entity) {
		entities.add(entity);
		// TODO - invoke from equip/remove action
	}

	/**
	 * Removes an entity that previously had an equipped glowing weapon.
	 * @param entity Entity to remove
	 */
	public void remove(Entity entity) {
		entities.remove(entity);
	}
}
