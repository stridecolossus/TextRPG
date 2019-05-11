package org.sarge.textrpg.object;

import java.util.Optional;
import java.util.function.Predicate;

import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Receptacle.Descriptor;
import org.sarge.textrpg.util.LocationCache;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Property;
import org.springframework.stereotype.Controller;

/**
 * Receptacle controller/helper.
 * @author Sarge
 */
@Controller
public class ReceptacleController {
	/**
	 * Global water receptacle.
	 */
	public static final Receptacle GLOBAL_WATER = new Receptacle(new Descriptor(ObjectDescriptor.of("global.water"), Liquid.WATER, Receptacle.INFINITE));

	/**
	 * Water receptacle matcher.
	 */
	private static final Predicate<Receptacle> MATCHER = matcher(Liquid.WATER);

	/**
	 * Global water receptacle cache.
	 */
	private final LocationCache<Receptacle> cache = new LocationCache<>(ReceptacleController::find);

	/**
	 * Creates a matcher for the given liquid.
	 * @param liquid Liquid
	 * @return Matching receptacle
	 */
	public static Predicate<Receptacle> matcher(Liquid liquid) {
		return rec -> {
			final Receptacle.Descriptor descriptor = rec.descriptor();
			return (descriptor.liquid() == liquid) && !rec.isEmpty();
		};
	}

	/**
	 * Finds a water source in the current location or the actors inventory.
	 * @param actor Actor
	 * @return Water receptacle
	 */
	public Optional<Receptacle> findWater(Entity actor) {
		// Check for global water source in the current location
		final Location loc = actor.location();
		if(loc.isProperty(Property.WATER)) {
			return Optional.of(GLOBAL_WATER);
		}

		// Otherwise find receptacle in current location or inventory
		return cache.find(loc)
			.or(() -> find(loc.contents()))
			.or(() -> find(actor.contents()));
	}

	/**
	 * Finds a global water receptacle in the given location.
	 * @param loc Location
	 * @return Global water receptacle
	 */
	private static Optional<Receptacle> find(Location loc) {
		return loc.contents()
			.select(Receptacle.class)
			.filter(rec -> rec.descriptor().isFixture())
			.filter(MATCHER)
			.findAny();
	}

	/**
	 * Finds a water receptacle in the given contents.
	 * @param contents Contents
	 * @return Water receptacle
	 */
	private static Optional<Receptacle> find(Contents contents) {
		return contents.select(Receptacle.class).filter(MATCHER).findAny();
	}
}
