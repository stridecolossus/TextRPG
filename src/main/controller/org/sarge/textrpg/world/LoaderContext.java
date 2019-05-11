package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.object.WorldObject;

/**
 * The <i>loader context</i> maintains transient information used during loading.
 */
public class LoaderContext extends AbstractObject {
	/**
	 * Stack of areas.
	 */
	public static class Stack extends AbstractObject {
		private final LinkedList<LoaderContext> stack = new LinkedList<>();

		/**
		 * @return Size of this stack
		 */
		public int size() {
			return stack.size();
		}

		/**
		 * @return Current parent area
		 */
		public LoaderContext parent() {
			return stack.peek();
		}

		/**
		 * Adds a new area.
		 * @param area Area wrapper
		 */
		public void push(LoaderContext area) {
			stack.push(area);
		}

		/**
		 * Removes the current area wrapper.
		 */
		public void pop() {
			stack.pop();
		}
	}

	private final Area area;
	private final Terrain terrain;
	private final Route route;
	private final Optional<Faction> faction;
	private final Map<String, DefaultLocation> junctions = new StrictMap<>();
	private final Map<String, WorldObject> objects = new StrictMap<>();
	private final Map<String, Grid> grids = new StrictMap<>();

	/**
	 * Constructor.
	 * @param area			Area descriptor
	 * @param terrain		Default terrain
	 * @param route			Default route
	 * @param faction		Optional faction for this area
	 */
	public LoaderContext(Area area, Terrain terrain, Route route, Faction faction) {
		this.area = notNull(area);
		this.terrain = notNull(terrain);
		this.route = notNull(route);
		this.faction = Optional.ofNullable(faction);
	}

	/**
	 * @return Area
	 */
	public Area area() {
		return area;
	}

	/**
	 * @return Default terrain
	 */
	public Terrain terrain() {
		return terrain;
	}

	/**
	 * @return Default route
	 */
	public Route route() {
		return route;
	}

	/**
	 * @return Current faction
	 */
	public Optional<Faction> faction() {
		return faction;
	}

	/**
	 * Looks up a junction connector.
	 * @param name Junction name
	 * @return Junction connector
	 */
	public Location junction(String name) {
		return junctions.get(name);
	}

	/**
	 * Registers a junction.
	 * @param junction Junction connector
	 */
	public void add(DefaultLocation junction) {
		junctions.put(junction.name(), junction);
	}

	/**
	 * Looks up a registered object.
	 * @param ref Object reference
	 * @return Referenced object or <tt>null</tt> if not found
	 */
	public WorldObject object(String ref) {
		return objects.get(ref);
	}

	/**
	 * Registers an object.
	 * @param ref		Reference
	 * @param obj 		Object to register
	 */
	public void add(String ref, WorldObject obj) {
		if(!obj.descriptor().isFixture()) throw new IllegalStateException("Only fixtures can be registered: " + obj);
		objects.put(ref, obj);
	}

	/**
	 * Looks up a registered grid.
	 * @param ref Grid reference
	 * @return Grid
	 */
	public Grid grid(String ref) {
		return grids.get(ref);
	}

	/**
	 * Registers a grid.
	 * @param ref		Grid reference
	 * @param grid		Grid
	 */
	public void add(String ref, Grid grid) {
		grids.put(ref, grid);
	}
}
