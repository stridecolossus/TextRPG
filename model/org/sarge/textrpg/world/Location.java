package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.Util;
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
	 * Default surfaces.
	 */
	private static final List<String> SURFACES = Arrays.asList("floor", "ceiling", "wall").stream().map(str -> "surface." + str).collect(toList());
	
	/**
	 * @return Surfaces of a location
	 */
	public static Stream<String> getSurfaces() {
		return SURFACES.stream();
	}

	private final String name;
	private final Area area;
	private final Terrain terrain;
	private final boolean water;
	private final Collection<String> decorations;
	
	private final Map<Direction, Exit> exits = new StrictMap<>();
	private final List<Tracks> tracks = new ArrayList<>();
	
	protected final Contents contents = new Contents();

	/**
	 * Constructor.
	 * @param name			Name of this location
	 * @param area			Parent area
	 * @param terrain		Terrain at this location
	 * @param water			Whether water is available at this location
	 * @param decoration	Decorations in this location
	 */
	public Location(String name, Area area, Terrain terrain, boolean water, Collection<String> decorations) {
		Check.notEmpty(name);
		Check.notNull(area);
		Check.notNull(terrain);
		this.name = name;
		this.area = area;
		this.terrain = terrain;
		this.water = water;
		this.decorations = new HashSet<>(decorations);
	}
	
	/**
	 * Copy constructor.
	 * @param loc Location
	 */
	protected Location(Location loc) {
		this(loc.name, loc.area, loc.terrain, loc.water, loc.decorations);
	}
	
	/**
	 * Copy constructor.
	 * @param name		Name
	 * @param loc		Location
	 */
	protected Location(String name, Location loc) {
		this(name, loc.area, loc.terrain, loc.water, loc.decorations);
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
		case INVERSE:	return new ReverseLink(link);
		case SIMPLE:	return Link.DEFAULT;
		default:		throw new RuntimeException();
		}
	}
	
	public final String getName() {
		return name;
	}
	
	@Override
	public String getParentName() {
		return NAME;
	}
	
	public Area getArea() {
		return area;
	}
	
	public Terrain getTerrain() {
		return terrain;
	}
	
	/**
	 * @return Whether water is available in this location
	 */
	public boolean isWaterAvailable() {
		return water;
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
		for(Direction dir : exits.keySet()) {
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
	public Contents getContents() {
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
	public final Parent getParent() {
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
				.filter(ContentsHelper.filter(actor))
				.filter(Thing.NOT_QUIET)
				.filter(t -> t != actor.getParent())
				.map(Thing::describe)
				.forEach(builder::add);
				
			// List portal controllers
			exits.values().stream()
				.map(Exit::getLink)
				.map(Link::getController)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(Thing.NOT_QUIET)
				.filter(actor::perceives)
				.map(Thing::describe)
				.forEach(builder::add);
		}
		
		// Build exits description
		final Description.Builder ex = new Description.Builder("exit.exits");
		for(Direction dir : getExits().keySet()) {
			// Check is visible to this actor
			final Exit exit = exits.get(dir);
			if(!exit.getLink().isVisible(actor)) continue;
			
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
		final String text = exit.getLink().describe(Description.wrap("exit." + name));
		if((exit.getLink().getRoute() != Route.RIVER) && (exit.getDestination().getTerrain() == Terrain.WATER)) {
			return Util.wrap(text, Route.RIVER.getLeftIcon());
		}
		else {
			return text;
		}
	}

	/**
	 * @param daylight Whether time-of-day is daylight
	 * @return Whether light is available in this location (natural or artificial)
	 */
	public final boolean isLightAvailable(boolean daylight) {
		final Terrain terrain = getTerrain();
		if(terrain.isDark()) {
			return isArtificialLightAvailable();
		}
		else {
			return daylight || terrain.isLit();
		}
	}
	
	/**
	 * @return Whether this location contains an artificial light
	 */
	public final boolean isArtificialLightAvailable() {
		return contents.stream().map(t -> t.getEmission(Emission.Type.LIGHT)).anyMatch(Optional::isPresent);
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
}
