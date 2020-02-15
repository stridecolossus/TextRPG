package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.springframework.stereotype.Component;

/**
 * Base-class for a location in the world.
 * @author Sarge
 */
public abstract class Location extends AbstractEqualsObject implements Parent {
	/**
	 * Descriptor for location properties.
	 */
	public static final class Descriptor extends AbstractEqualsObject {
		private final String name;
		private final Terrain terrain;
		private final byte props;

		/**
		 * Constructor.
		 * @param name			Location name
		 * @param terrain		Terrain
		 * @param props			Properties
		 */
		private Descriptor(String name, Terrain terrain, byte props) {
			this.name = notEmpty(name);
			this.terrain = notNull(terrain);
			this.props = props;
		}

		/**
		 * Copy constructor that over-rides the terrain.
		 * @param descriptor		Descriptor
		 * @param terrain			Terrain
		 */
		public Descriptor(Descriptor descriptor, Terrain terrain) {
			this(descriptor.name, terrain, descriptor.props);
		}

		/**
		 * Convenience constructor for a default descriptor.
		 * @param name Location name
		 */
		public Descriptor(String name) {
			this(name, Terrain.GRASSLAND, (byte) 0);
		}

		/**
		 * @return Location name
		 */
		public String name() {
			return name;
		}

		/**
		 * @return Terrain
		 */
		public Terrain terrain() {
			return terrain;
		}

		/**
		 * Builder for a location descriptor.
		 */
		public static class Builder {
			private String name;
			private Terrain terrain = Terrain.GRASSLAND;
			private final Set<Property> props = new StrictSet<>();

			/**
			 * Sets the location name.
			 * @param name Location name
			 */
			public Descriptor.Builder name(String name) {
				this.name = name;
				return this;
			}

			/**
			 * Sets the terrain at this location.
			 * @param terrain Terrain
			 */
			public Descriptor.Builder terrain(Terrain terrain) {
				this.terrain = terrain;
				return this;
			}

			/**
			 * Add a property to this location.
			 * @param prop Property
			 */
			public Builder property(Property prop) {
				props.add(prop);
				return this;
			}

			/**
			 * @return New location descriptor
			 */
			public Descriptor build() {
				return new Descriptor(name, terrain, Property.toBitField(props));
			}
		}
	}

	// Properties
	private final Descriptor descriptor;
	private transient Percentile light;
//	protected ExitMap exits;

	// Contents
	private final Contents contents = new Contents();
	private final ArrayList<Tracks> tracks = new ArrayList<>();

	/**
	 * Constructor.
	 * @param descriptor		Descriptor
//	 * @param exits				Optional exits
	 */
	protected Location(Descriptor descriptor) { // , ExitMap exits) {
		this.descriptor = notNull(descriptor);
//		this.exits = exits;
	}

	/**
	 * @return Name of this location
	 */
	@Override
	public String name() {
		return descriptor.name();
	}

	/**
	 * @return Descriptor of this location
	 */
	protected Descriptor descriptor() {
		return descriptor;
	}

	/**
	 * @return Terrain at this location
	 */
	public Terrain terrain() {
		return descriptor.terrain();
	}

	@Override
	public final Parent parent() {
		return null;
	}

	/**
	 * @return Parent area of this location
	 */
	public abstract Area area();

	/**
	 * @param p Property
	 * @return Whether this location has the given property
	 * @see Area#isProperty(Property)
	 */
	public boolean isProperty(Property p) {
		return area().isProperty(p) ^ p.isProperty(descriptor.props);
	}

	/**
	 * @return Whether this is a water location
	 */
	public boolean isWater() {
		return terrain() == Terrain.WATER;
	}

	/**
	 * @return Whether this location is frozen
	 * @see Weather#isFrozen()
	 */
	public boolean isFrozen() {
		return area().weather().isFrozen();
	}

	/**
	 * Tests whether moving to the given location results in an area transition.
	 * @param next Next location
	 * @return Whether transitions to a new area
	 */
	public boolean isTransition(Location next) {
		return this.area() != next.area();
	}

	/**
	 * @return Exits <b>from</b> this location
	 */
	public abstract ExitMap exits();

<<<<<<< Updated upstream
	/**
	 * Adds an exit from this location.
	 * @param exit Exit
	 */
	protected void add(Exit exit) {
		exits.add(exit);
	}

	/**
	 * Invoked on completion of the linkage phase for this location.
	 * @see Linker#link()
	 */
	protected void complete() {
		exits = ExitMap.of(exits);
=======
	protected abstract void add(Exit exit);

	protected void compact() {

>>>>>>> Stashed changes
	}

//	/**
//	 * @return Exits <b>from</b> this location
//	 */
//	public ExitMap exits() {
//		return exits;
//	}
//
//	/**
//	 * Adds an exit from this location.
//	 * @param exit Exit
//	 */
//	protected void add(Exit exit) {
////		exits.add(exit);
//	}
//
//	/**
//	 * Invoked on completion of the linkage phase for this location.
//	 * @see Linker#link()
//	 */
//	protected void complete() {
////		exits = ExitMap.of(exits);
//	}

	@Override
	public Contents contents() {
		return contents;
	}

	/**
	 * @return Tracks in this location
	 */
	public Stream<Tracks> tracks() {
		return tracks.stream();
	}

	/**
	 * Adds a set of tracks to this location.
	 * @param t Tracks to add
	 */
	void add(Tracks t) {
		tracks.add(t);
	}

	/**
	 * Removes a set of tracks from this location.
	 * @param t Tracks to remove
	 */
	void remove(Tracks t) {
		assert tracks.contains(t);
		tracks.remove(t);
	}

	/**
	 * Determines the maximum intensity of the given emission in this location.
	 * @param emission Type of emission
	 * @return Intensity
	 */
	public Percentile emission(Emission emission) {
		if(emission == Emission.LIGHT) {
			if(light == null) {
				light = Thing.max(emission, contents.stream());
			}
			return light;
		}
		else {
			return Thing.max(emission, contents.stream());
		}
	}

	/**
	 * Convenience method to broadcast a general alert notification to entities in this location.
	 * @param actor			Actor
	 * @param alert			Alert
	 * @See {@link Actor#broadcast(Actor, Description, Stream)}
	 */
	public void broadcast(Actor actor, Description alert) {
		Actor.broadcast(actor, alert, contents.stream());
	}

	@Override
	public boolean notify(ContentStateChange notification) {
		switch(notification.type()) {
		case LIGHT:
			// Flag light-level as dirty
			light = null;
			break;

		case OTHER:
			// Broadcast general notifications
			broadcast(null, notification.describe());
		}

		return false;
	}

	@Override
	public String toString() {
		return name();
	}

	/**
	 * The location linker patches exits between locations after all locations have been loaded.
	 * <p>
	 * The linkage process is as follows:
	 * <ol>
	 * <li>Locations are constructed using the various builders</li>
	 * <li>Exits are specified using {@link #add(LinkedExit)} which refers to the destination location by <b>name</b></li>
	 * <li>Locations that are available as a destination (known as a <i>connector</i>) are registered with the linker using {@link #add(Location)}</li>
	 * <li>Finally the {@link #link()} method constructs <b>all</b> concrete exits</li>
	 * </ol>
	 * <p>
	 * Note that an {@link LinkedExit} also specifies the properties of bi-directional exits.
	 * <p>
	 * Usage:
	 * <pre>
	 *  // Create linker
	 *  final Linker linker = new Linker();
	 *
	 *  // Create a location
	 *  final Location loc = ...
	 *
	 *  // Create an exit
	 *  final ExitWrapper exit = new ExitWrapper(loc, ..., "dest");
	 *  linker.add(exit);
	 *
	 *  // Register location as a connector (if required)
	 *  linker.add(loc);
	 *
	 *  // Link all locations
	 *  linker.link();
	 * </pre>
	 * <p>
	 * @see LinkedExit
	 */
	@Component
	public static class Linker {
		private final List<LinkedExit> exits = new ArrayList<>();
		private final Map<String, Location> connectors = new StrictMap<>();

		/**
		 * Looks up a connector.
		 * @param name Connector name
		 * @return Connector
		 */
		public Location connector(String name) {
			final Location connector = connectors.get(name);
			if(connector == null) throw new IllegalArgumentException("Unknown connector: " + name);
			return connector;
		}

		/**
		 * Adds an exit to be patched.
		 * @param exit Exit wrapper
		 */
		public void add(LinkedExit exit) {
			assert !exits.contains(exit);
			exits.add(exit);
		}

		/**
		 * Register a location as a connector.
		 * @param loc Location
		 */
		public void add(Location loc) {
			connectors.put(loc.name(), loc);
		}

		/**
		 * Patches <b>all</b> exits.
		 * <p>
		 * Notes:
		 * <ul>
		 * <li>Constructs bi-directional exits according to the {@link LinkedExit.ReversePolicy} of the exit</li>
		 * <li>Invokes {@link Location#complete()} on all connectors <b>after</b> the completion of the linking phase</li>
		 * </ul>
		 * @throws IllegalArgumentException for an exit-wrapper with an unknown destination (i.e. no matching connector)
		 * @see Location#complete()
		 */
		public void link() {
			// Patch links
			for(LinkedExit wrapper : exits) {
				// Lookup destination
				final Location start = wrapper.start();
				final Location end;
				if(wrapper.destination().equals("self")) {
					end = start;
				}
				else {
					end = connector(wrapper.destination());
				}

				// Create exit descriptor
				final Exit exit = wrapper.exit(end);
				start.add(exit);

				// Create reverse exit
				if(wrapper.policy() != LinkedExit.ReversePolicy.ONE_WAY) {
					final Exit reverse = wrapper.reverse();
					end.add(reverse);
				}
			}

			// Complete linking
			connectors.values().forEach(Location::complete);
		}
	}
}
