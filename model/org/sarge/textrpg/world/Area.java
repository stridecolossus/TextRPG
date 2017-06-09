package org.sarge.textrpg.world;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.object.LootFactory;

/**
 * Group of locations.
 * @author Sarge
 */
public final class Area {
	/**
	 * Root area.
	 */
	public static final Area ROOT = new Area();
	
	/**
	 * Resources that can be gathered in this area.
	 */
	public enum Resource {
		HERBS,
		WOOD,
		FISH
	}

	/**
	 * Ambient event descriptor.
	 */
	public static class Ambient {
		private final String name;
		private final long period;
		private final boolean repeat;

		/**
		 * Constructor.
		 * @param name		Name
		 * @param period	Period (ms)
		 * @param repeat	Whether this is a repeating event
		 */
		public Ambient(String name, long period, boolean repeat) {
			Check.notEmpty(name);
			Check.oneOrMore(period);
			this.name = name;
			this.period = period;
			this.repeat = repeat;
		}
		
		public String getName() {
			return name;
		}
		
		public long getPeriod() {
			return period;
		}
		
		public boolean isRepeating() {
			return repeat;
		}
		
		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}
	
	private final String name;
	private final Area parent;
	private final Terrain terrain;
	private final Route route;
	private final Map<Resource, LootFactory> resources;
	private final Collection<Ambient> ambient;
	// TODO
	// - creature spawns

	/**
	 * Constructor.
	 * @param name			Name of this area
	 * @param parent		Parent area
	 * @param route			Default route-type in this area
	 * @param terrain		Default terrain in this area
	 * @param resources		Loot-factories for resources in this area
	 */
	public Area(String name, Area parent, Terrain terrain, Route route, Map<Resource, LootFactory> resources, Collection<Ambient> ambient) {
		Check.notEmpty(name);
		Check.notNull(parent);
		Check.notNull(terrain);
		Check.notNull(route);
		this.name = name;
		this.parent = parent;
		this.terrain = terrain;
		this.route = route;
		this.resources = new HashMap<>(resources);
		this.ambient = new ArrayList<>(ambient);
	}

	/**
	 * {@link #ROOT} constructor.
	 */
	private Area() {
		this.name = "root";
		this.parent = null;
		this.terrain = Terrain.GRASSLAND;
		this.route = Route.NONE;
		this.resources = Collections.emptyMap();
		this.ambient = Collections.emptyList();
	}
	
	/**
	 * @return Area name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Parent area or <tt>null</tt> if root
	 * @see Area#ROOT
	 */
	public Area getParent() {
		return parent;
	}
	
	/**
	 * @return Default terrain in this area
	 */
	public Terrain getTerrain() {
		return terrain;
	}
	
	/**
	 * @return Default route in this area
	 */
	public Route getRouteType() {
		return route;
	}

	/**
	 * @param res Resource type
	 * @return Factory for the given resource
	 */
	public Optional<LootFactory> getResource(Resource res) {
		return Optional.ofNullable(resources.get(res));
	}

	/**
	 * @return Ambient events in this area
	 */
	public Stream<Ambient> getAmbientEvents() {
		return ambient.stream();
	}
	
	/**
	 * @return Slash-delimited path from this area to the {@link #ROOT}.
	 */
	public String path() {
		final List<Area> path = new ArrayList<>();
		Area area = this;
		while(area.parent != null) {
			path.add(area);
			area = area.parent;
		}
		return path.stream().map(Area::getName).collect(joining("/"));
	}
	
	@Override
	public String toString() {
		return name;
	}
}
