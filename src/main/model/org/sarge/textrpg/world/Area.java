package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.View;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.util.NameStore;

/**
 * Area of the world.
 * <p>
 * An area defines the common attributes of one-or-more locations in the world:
 * <ul>
 * <li>resources that can be found or gathered in the area, see {@link #resource(Resource)</li>
 * <li>{@link #ambient()} events that occur when an entity is within the area</li>
 * <li>none-or-more {@link #view()} available from this area</li>
 * <li>the {@link #weather()} model for the area</li>
 * <li>a name-store for objects, entities and locations in the area</li>
 * </ul>
 * Notes:
 * <ul>
 * <li>Resources and weather are optional properties that delegate to the {@link #parent()} area if not specified</li>
 * <li>An area does <b>not</b> explicitly enumerate the locations in that area, see {@link Location#area()}</li>
 * <li>{@link #ROOT} is the ancestor of <b>all</b> areas</li>
 * </ul>
 * @author Sarge
 */
public final class Area {
	/**
	 * Root area.
	 */
	public static final Area ROOT = new Area();

	private final String name;
	private final Area parent;
	private final Set<Property> props;
	private final Map<String, LootFactory> resources;
	private final Map<Direction, View> views;
	private final Optional<Weather> weather;
	private final Optional<AmbientEvent> ambient;
	private final NameStore store;

	/**
	 * Constructor.
	 * @param name			Name of this area
	 * @param parent		Parent area
	 * @param props			Default location properties for this area
	 * @param resources		Loot-factories for resources in this area
	 * @param views			Optional views from locations in this area
	 * @param weather		Optional weather for this area
	 * @param ambient		Optional ambient event in this area
	 * @param store			Name-store for this area
	 */
	private Area(String name, Area parent, Set<Property> props, Map<String, LootFactory> resources, Map<Direction, View> views, Weather weather, AmbientEvent ambient, NameStore store) {
		if(!props.stream().allMatch(Property::isAreaProperty)) throw new IllegalArgumentException("Invalid area property");
		this.name = notEmpty(name);
		this.parent = notNull(parent);
		this.props = Set.copyOf(props);
		this.resources = Map.copyOf(resources);
		this.views = Map.copyOf(views);
		this.weather = Optional.ofNullable(weather);
		this.ambient = Optional.ofNullable(ambient);
		this.store = NameStore.of(store, parent.store);
	}

	/**
	 * {@link #ROOT} constructor.
	 */
	private Area() {
		this.name = "root";
		this.parent = null;
		this.props = Set.of();
		this.resources = Map.of();
		this.views = Map.of();
		this.weather = Optional.of(Weather.NONE);
		this.ambient = Optional.empty();
		this.store = NameStore.EMPTY;
	}

	/**
	 * @return Area name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Parent area or <tt>null</tt> if root
	 * @see Area#ROOT
	 */
	public Area parent() {
		return parent;
	}

	/**
	 * @param p Property
	 * @return Whether this area has the given default property
	 */
	public boolean isProperty(Property p) {
		return props.contains(p);
	}

	/**
	 * Looks up the factory for the given resource in this area.
	 * @param res Resource type
	 * @return Factory for the given resource
	 */
	public Optional<LootFactory> resource(String res) {
		return find(area -> Optional.ofNullable(area.resources.get(res)));
	}

	/**
	 * @param dir Direction
	 * @return View in the given direction from vantage-points in this area
	 * @see Property#VANTAGE_POINT
	 */
	public Optional<View> view(Direction dir) {
		return Optional.ofNullable(views.get(dir));
	}

	/**
	 * @return Ambient event in this area
	 */
	public Optional<AmbientEvent> ambient() {
		return ambient;
	}

	/**
	 * @return Current weather in this area or its ancestors
	 */
	public Weather weather() {
		return find(area -> area.weather).get();
	}

	/**
	 * @return Names-store for this area
	 */
	public NameStore store() {
		return store;
	}

	/**
	 * Helper - Find a property in this area or its ancestors.
	 * @param mapper Maps an area to a property
	 * @return Property
	 * @param <T> Property type
	 */
	private <T> Optional<T> find(Function<Area, Optional<T>> mapper) {
		Area area = this;
		while(true) {
			// Stop at root area
			if(area == ROOT) {
				return mapper.apply(area);
			}

			// Find value in this area
			final Optional<T> value = mapper.apply(area);
			if(value.isPresent()) {
				return value;
			}

			// Otherwise walk to parent area
			area = area.parent;
		}
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Builder for an area.
	 */
	public static class Builder {
		private final String name;
		private Area parent = Area.ROOT;
		private final Set<Property> props = new StrictSet<>();
		private final Map<String, LootFactory> resources = new StrictMap<>();
		private final Map<Direction, View> views = new StrictMap<>();
		private Weather weather;
		private AmbientEvent ambient;
		private NameStore store = NameStore.EMPTY;

		/**
		 * Constructor.
		 * @param name Area name
		 */
		public Builder(String name) {
			this.name = Check.notEmpty(name);
		}

		/**
		 * Sets the parent of this area.
		 * @param parent Parent area
		 */
		public Builder parent(Area parent) {
			this.parent = Check.notNull(parent);
			return this;
		}

		/**
		 * Adds a default location property for this area.
		 * @param p Property
		 */
		public Builder property(Property p) {
			props.add(p);
			return this;
		}

		/**
		 * Adds a resource factory to this area.
		 * @param res			Resource type
		 * @param factory		Factory
		 */
		public Builder resource(String res, LootFactory factory) {
			this.resources.put(res, factory);
			return this;
		}

		/**
		 * Adds a view in this area.
		 * @param dir		Direction
		 * @param view 		View
		 */
		public Builder view(Direction dir, View view) {
			views.put(dir, view);
			return this;
		}

		/**
		 * Sets the weather in this area.
		 * @param weather Weather descriptor
		 */
		public Builder weather(Weather weather) {
			this.weather = weather;
			return this;
		}

		/**
		 * Sets the ambient event for this area.
		 * @param event Ambient event
		 */
		public Builder ambient(AmbientEvent ambient) {
			this.ambient = ambient;
			return this;
		}

		/**
		 * Sets the name-store for this area.
		 * @param store Name-store
		 */
		public Builder store(NameStore store) {
			this.store = store;
			return this;
		}

		/**
		 * @return New area
		 */
		public Area build() {
			return new Area(name, parent, props, resources, views, weather, ambient, store);
		}
	}
}
