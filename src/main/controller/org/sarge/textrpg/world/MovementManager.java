package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sarge.lib.collection.LoopIterator;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.Randomiser;

/**
 * Movement manager for AI entities.
 * @author Sarge
 */
public interface MovementManager {
	/**
	 * Movement manager for a static entity.
	 */
	MovementManager IDLE = loc -> {
		throw new UnsupportedOperationException();
	};

	/**
	 * Random movement manager.
	 */
	MovementManager RANDOM = loc -> {
		final ExitMap exits = loc.exits();
		if(exits.isEmpty()) return Optional.empty();
		final var available = exits.stream().collect(toList());
		return Optional.of(Randomiser.select(available));
	};

	/**
	 * Selects the next exit.
	 * @param loc Current location
	 * @return Exit
	 */
	Optional<Exit> next(Location loc);

	/**
	 * Creates a movement manager for preferred terrain.
	 * @param terrain Preferred terrain(s)
	 * @return Movement manager
	 */
	static MovementManager terrain(Set<Terrain> terrain) {
		return loc -> loc.exits().stream()
			.filter(e -> terrain.contains(e.destination().terrain()))
			.findAny();
	}

	/**
	 * Creates a movement manager that follows the given path.
	 * @param path Path
	 * @return Path movement manager
	 */
	static MovementManager path(List<Direction> path) {
		final LoopIterator<Direction> iterator = new LoopIterator<>(path, LoopIterator.Strategy.LOOP);
		return loc -> loc.exits().find(iterator.next());
	}

	/**
	 * Loads a custom movement manager.
	 */
	static MovementManager load(Element xml) {
		final String type = xml.attribute("type").toText();
		switch(type) {
		case "random":
			return MovementManager.RANDOM;

		case "terrain":
			final Set<Terrain> terrain = xml.children().map(Element::name).map(Terrain.CONVERTER).collect(toSet());
			return MovementManager.terrain(terrain);

		case "path":
			final List<Direction> path = Direction.path(xml.attribute("path").toText());
			return MovementManager.path(path);

		default:
			throw xml.exception("Invalid movement manager: " + type);
		}
	}
}
