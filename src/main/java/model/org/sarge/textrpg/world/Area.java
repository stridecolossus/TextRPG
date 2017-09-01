package org.sarge.textrpg.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
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
	public static final class Ambient {
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

		/**
		 * @return Event identifier
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return Period (ms)
		 */
		public long getPeriod() {
			return period;
		}

		/**
		 * @return Whether this is a repeating event
		 */
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
	private final Map<Resource, LootFactory> resources;
	private final Collection<Ambient> ambient;

	/**
	 * Constructor.
	 * @param name			Name of this area
	 * @param parent		Parent area
	 * @param resources		Loot-factories for resources in this area
	 */
	public Area(String name, Area parent, Map<Resource, LootFactory> resources, Collection<Ambient> ambient) {
		Check.notEmpty(name);
		Check.notNull(parent);
		this.name = name;
		this.parent = parent;
		this.resources = new HashMap<>(resources);
		this.ambient = new ArrayList<>(ambient);
	}

	/**
	 * {@link #ROOT} constructor.
	 */
	private Area() {
		this.name = "root";
		this.parent = null;
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
	 * @return Slash-delimited path from this area to the {@link #ROOT}
	 */
	public Stream<Area> path() {
		final List<Area> path = new ArrayList<>();
		Area area = this;
		while(area.parent != null) {
			path.add(area);
			area = area.parent;
		}
		return path.stream();
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
		private final Map<Resource, LootFactory> resources = new StrictMap<>();
		private final Collection<Ambient> ambient = new StrictSet<>();

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
		 * Adds a resource factory to this area.
		 * @param res			Resource type
		 * @param factory		Factory
		 */
		public Builder resource(Resource res, LootFactory factory) {
			this.resources.put(res, factory);
			return this;
		}

		/**
		 * Adds an ambient event to this area.
		 * @param event Ambient event
		 */
		public Builder parent(Ambient event) {
			this.ambient.add(event);
			return this;
		}

		/**
		 * Constructs an area.
		 * @return New area
		 */
		public Area build() {
			return new Area(name, parent, resources, ambient);
		}
	}
}
