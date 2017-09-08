package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StringUtil;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.world.LinkWrapper.ReversePolicy;

/**
 * Location in the world.
 * @author Sarge
 */
public class Location implements Parent {
	/**
	 * Parent identifier.
	 */
	public static final String NAME = "location";

	/**
	 * Properties of this location.
	 */
	public enum Property {
	    /**
	     * Location has drinking water.
	     */
	    WATER,

	    /**
	     * Location is naturally light/dark (over-rides terrain).
	     * @see Terrain#isDark()
	     */
	    LIGHT,

	    /**
	     * Location can be fished.
	     */
	    FISH;

	    private final byte bit;

        private Property() {
            this.bit = (byte) (1 << ordinal());
        }
	}

	/**
	 * Default surfaces.
	 */
	private static final List<String> SURFACES = Arrays.asList("floor", "ceiling", "wall").stream().map(str -> "surface." + str).collect(toList());

	/**
	 * @return Surfaces of a location
	 */
	public static Stream<String> getSurfaces() {
		return SURFACES.stream();
	}

//	/**
//	 * Caches locations containing artificial lights.
//	 * TODO
//	 * - replace with soft-ref set?
//	 * - http://www.javaspecialists.eu/archive/Issue098.html
//	 * - thread-safe
//	 * - OR use a 'synthetic' bit in PROPS
//	 */
//	private static final byte LIGHT_CACHE_BIT = 1 << 8; // TODO

	private final String name;
	private final Area area;
	private final Terrain terrain;
	private final byte props;
	private final Collection<String> decorations;
	private final Map<Direction, Exit> exits = new StrictMap<>();
	private final List<Tracks> tracks = new ArrayList<>();

	protected final Contents contents = new Contents();

	/*{
	    @Override
	    public void add(Thing obj) {
            clearCache();
	        super.add(obj);
	    }

	    @Override
        protected void remove(Thing obj) {
            clearCache();
	        super.remove(obj);
	    }

	    private void clearCache() {
            LIGHT_CACHE.remove(Location.this);
	    }
	};
	*/

	/**
	 * Constructor.
	 * @param name			Name of this location
	 * @param area			Parent area
	 * @param terrain		Terrain at this location
	 * @param props         Properties of this location
	 * @param decoration	Decorations in this location
	 */
	public Location(String name, Area area, Terrain terrain, Set<Property> props, Collection<String> decorations) {
		Check.notEmpty(name);
		Check.notNull(area);
		Check.notNull(terrain);
		this.name = name;
		this.area = area;
		this.terrain = terrain;
		this.props = (byte) props.stream().mapToInt(p -> p.bit).reduce((a, b) -> a | b).orElse(0);
		this.decorations = new HashSet<>(decorations);
	}

	// TODO - 1. protected ctors messy 2. deal with properties

	/**
	 * Copy constructor.
	 * @param loc Location
	 */
	protected Location(Location loc) {
		this(loc.name, loc.area, loc.terrain, /*loc.props*/Collections.emptySet(), loc.decorations);
	}

	/**
	 * Copy constructor.
	 * @param name		Name
	 * @param loc		Location
	 */
	protected Location(String name, Location loc) {
		this(name, loc.area, loc.terrain, /*loc.props*/null, loc.decorations);
	}

	/**
	 * Adds a new link.
	 * @param dir		Direction <b> from</b> this location
	 * @param link		Link descriptor
	 * @param policy	Reverse link policy
	 * @throws IllegalArgumentException for a duplicated link
	 */
	public /* TODO protected*/ void add(LinkWrapper wrapper) {
		// Add link
		final Link link = wrapper.getLink();
		final Direction dir = wrapper.getDirection();
		final Location dest = wrapper.getDestination();
		if(exits.containsKey(dir)) throw new IllegalArgumentException(String.format("Duplicate link: dir=%s loc=%s", dir, this));
		exits.put(dir, new Exit(dest, wrapper.getLink()));

		// Add reverse link
		final ReversePolicy policy = wrapper.getReversePolicy();
		if(policy != ReversePolicy.ONE_WAY) {
			final Direction reverse = wrapper.getReverseDirection();
			if(dest.exits.containsKey(reverse)) throw new IllegalArgumentException(String.format("Duplicate reverse link: dir=%s loc=%s dest=%s", reverse, this, dest));
			dest.exits.put(reverse, new Exit(this, dest.invert(link, policy)));
		}
	}

	/**
	 * Creates a reverse link.
	 */
	protected Link invert(Link link, ReversePolicy reverse) {
		switch(reverse) {
		case INVERSE:	return link;
		case SIMPLE:	return Link.DEFAULT;
		default:		throw new RuntimeException();
		}
	}

	public final String getName() {
		return name;
	}

	@Override
	public String parentName() {
		return NAME;
	}

	public Area getArea() {
		return area;
	}

	public Terrain getTerrain() {
		return terrain;
	}

	/**
	 * @param p Property
	 * @return Whether this location has the given property
	 */
	public boolean isProperty(Property p) {
	    return (props & p.bit) > 0;
	}

	/**
	 * @return Whether this location can be reached, default is <tt>true</tt>
	 */
	public boolean isOpen() {
		return true;
	}

	/**
	 * @return Exits <i>from</i> this location ordered by direction
	 */
	public Map<Direction, Exit> getExits() {
		final Map<Direction, Exit> map = new HashMap<>();
		for(final Direction dir : exits.keySet()) {
			final Exit exit = exits.get(dir);
			if(exit.getDestination().isOpen()) {
				map.put(dir, exit);
			}
		}
		return map;
	}

	/**
	 * @return Location <i>under</i> this for a floor-less location
	 */
	public Location getBase() {
		return this;
	}

	@Override
	public Contents contents() {
		return contents;
	}

	/**
	 * @return Tracks in this location
	 */
	public Stream<Tracks> getTracks() {
		return tracks.stream();
	}

	/**
	 * Removes a set of tracks.
	 * @param tracks Tracks to remove
	 */
	protected void remove(Tracks tracks) {
		this.tracks.remove(tracks);
	}

	/**
	 * @return Decorations
	 */
	public Stream<String> getDecorations() {
		return decorations.stream();
	}

	@Override
	public final Parent parent() {
		return null;
	}

	/**
	 * Describes this location.
	 * @param daylight		Whether time-of-day is daylight
	 * @param actor			Actor
	 * @return Description of this location
	 */
	public final Description describe(boolean daylight, Actor actor) {
		// Check whether actor can see anything
		final boolean light = isLightAvailable(daylight);

		// Start description
		final Description.Builder builder = new Description.Builder(light ? "location.description" : "location.description.dark");
		describe(builder);

		if(light) {
			// List contents
			contents.stream()
				.filter(ContentsHelper.filter(actor))			// Ignore self and check is perceived
				.filter(Thing.NOT_QUIET)
				.filter(t -> t != actor.parent())			// Ignore furniture/vehicle
				.map(Thing::describe)
				.forEach(builder::add);

			// List portal controllers
			exits.values().stream()
				.map(Exit::getLink)
				.map(Link::controller)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(Thing.NOT_QUIET)
				.filter(actor::perceives)
				.map(Thing::describe)
				.forEach(builder::add);
		}

		// Build exits description
		final Description.Builder ex = new Description.Builder("exit.exits");
		for(final Direction dir : getExits().keySet()) {
			// Check is visible to this actor
			final Exit exit = exits.get(dir);
			if(!exit.getLink().isVisible(actor)) {
				continue;
			}

			// Add exit
			final String name = dir.name().toLowerCase();
			ex.add(name, describe(name, exit));
		}
		builder.add(ex.buildNone());

		// Build location description
		return builder.build();
	}

	/**
	 * Adds description attributes.
	 * @param builder Description builder
	 */
	protected void describe(Description.Builder builder) {
		builder.wrap("name", name);
		builder.wrap("area", area.getName());
	}

	/**
	 * Helper - Generates the wrapped exit description.
	 * @param dir		Link direction
	 * @param exit		Exit descriptor
	 * @return Exit description
	 */
	protected static final String describe(String name, Exit exit) {
		final String text = exit.getLink().describe(Description.wrap("exit", name));
		if(isWaterDestination(exit)) {
			return StringUtil.wrap(text, Route.RIVER.getLeftIcon());
		}
		else {
			return text;
		}
	}

	private static boolean isWaterDestination(Exit exit) {
		return (exit.getLink().route() != Route.RIVER) && (exit.getDestination().getTerrain() == Terrain.WATER);
	}

	/**
	 * @param daylight Whether time-of-day is daylight
	 * @return Whether light is available in this location (natural or artificial)
	 * TODO - should this return light-level?
	 */
	public final boolean isLightAvailable(boolean daylight) {
		final Terrain terrain = getTerrain();
		final boolean light = isProperty(Property.LIGHT);
		if(terrain.isDark()) {
			return light || isArtificialLightAvailable();
		}
		else {
			return daylight && light;
		}
	}

	/**
	 * @return Whether this location contains an artificial light
	 */
	public final boolean isArtificialLightAvailable() {
        return contents.stream().map(t -> t.emission(Emission.Type.LIGHT)).anyMatch(Optional::isPresent);
        /*
	    if(LIGHT_CACHE.contains(this)) {
	        return true;
	    }
	    else {
	        final boolean light = contents.stream().map(t -> t.getEmission(Emission.Type.LIGHT)).anyMatch(Optional::isPresent);
	        if(light) {
	            LIGHT_CACHE.add(this);
	        }
	        return light;
	    }
	    */
	}

	/**
	 * Broadcasts a notification to all entities in this location.
	 * @param actor		Actor
	 * @param n			Notification
	 * @see Thing#isSentient()
	 * @see Thing#alert(Notification)
	 */
	public final void broadcast(Actor actor, Notification n) {
		contents.stream()
			.filter(Thing::isSentient)
			.filter(Actor.filter(actor))
			.forEach(t -> t.alert(n));
	}

	@Override
	public String toString() {
		return name;
	}

//	/**
//	 * Builder for a location.
//	 */
//	public static class Builder {
//	    private final String name;
//	    private Area area = Area.ROOT;
//	    private Terrain terrain = Terrain.GRASSLAND;
//	    private boolean water;
//	    private final Collection<String> decorations = new StrictSet<>();
//	    private final Map<Direction, Exit> exits = new StrictMap<>();
//
//	    /**
//	     * Constructor.
//	     * @param name Location name
//	     */
//	    public Builder(String name) {
//            this.name = notEmpty(name);
//        }
//
//	    /**
//	     * Sets the parent area of this location.
//	     * @param area Area
//	     */
//	    public Builder area(Area area) {
//	        this.area = notNull(area);
//	        return this;
//	    }
//
//	    /**
//	     * Sets the terrain of this location.
//	     * @param terrain Terrain
//	     */
//        public Builder terrain(Terrain terrain) {
//            this.terrain = notNull(terrain);
//            return this;
//        }
//
//        // TODO
//        // - flags
//        // - decorations
//
//        /**
//         * Adds a link <i>from</i> this location.
//         * @param wrapper Link descriptor
//         */
//        public Builder link(LinkWrapper wrapper) {
//            // Add link
//            final Link link = wrapper.getLink();
//            final Direction dir = wrapper.getDirection();
//            final Location dest = wrapper.getDestination();
//            if(exits.containsKey(dir)) throw new IllegalArgumentException(String.format("Duplicate link: dir=%s loc=%s", dir, this));
//            exits.put(dir, new Exit(dest, wrapper.getLink()));
//
//            // Add reverse link
//            final ReversePolicy policy = wrapper.getReversePolicy();
//            if(policy != ReversePolicy.ONE_WAY) {
//                final Direction reverse = wrapper.getReverseDirection();
//                if(dest.exits.containsKey(reverse)) throw new IllegalArgumentException(String.format("Duplicate reverse link: dir=%s loc=%s dest=%s", reverse, this, dest));
//                dest.exits.put(reverse, new Exit(this, dest.invert(link, policy)));
//            }
//
//            return this;
//        }
//
//	    public Location build() {
//	        return new Location(name, area, terrain, water, decorations);
//	    }
//	}
}
