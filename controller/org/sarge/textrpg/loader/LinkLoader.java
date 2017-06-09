package org.sarge.textrpg.loader;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.common.*;
import org.sarge.textrpg.object.*;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextNode;
import org.sarge.textrpg.world.*;
import org.sarge.textrpg.world.LinkWrapper.ReversePolicy;

/**
 * Loader for links.
 * @author Sarge
 */
public class LinkLoader {
	private static final Logger LOG = Logger.getLogger(LinkLoader.class.getName());
	
	private static final Converter<ReversePolicy> REVERSE = Converter.enumeration(ReversePolicy.class);
	
	private final ObjectLoaderAdapter objectLoader;
	private final World world;
	
	private Converter<Percentile> difficultyConverter = Percentile.CONVERTER;
	
	private Duration forget = Duration.ofMinutes(5);

	/**
	 * Constructor.
	 * @param objectLoader		Loader for objects
	 * @param entityLoader		Loader for entities
	 * @param world				World model
	 */
	public LinkLoader(ObjectLoaderAdapter objectLoader, World world) {
		Check.notNull(objectLoader);
		this.objectLoader = objectLoader;
		this.world = world;
	}
	
	public void setDifficultyConverter(Converter<Percentile> difficultyConverter) {
		this.difficultyConverter = difficultyConverter;
	}
	
	public void setDefaultForgetPeriod(Duration forget) {
		Check.notNull(forget);
		this.forget = forget;
	}

	/**
	 * Loads a link-wrapper.
	 */
	public LinkWrapper loadLinkWrapper(TextNode node, Route route, Location loc) {
		// Lookup destination
		final String name = node.getString("dest", null);
		final boolean self = name.equals("self");
		final Location dest;
		if(self) {
			dest = loc;
		}
		else {
			dest = world.getLocations().find(name);
			if(dest == null) throw node.exception("Unknown destination: " + name);
		}

		// Create link
		final Direction dir = node.getAttribute("dir", null, Direction.CONVERTER);
		final Link link = loadLink(node, route, loc, dest);
		
		// Wrap link
		final ReversePolicy def = self ? ReversePolicy.ONE_WAY : ReversePolicy.INVERSE;
		final ReversePolicy policy = node.getAttribute("reverse", def, REVERSE);
		final Direction reverse = node.getAttribute("reverse-dir", dir.reverse(), Direction.CONVERTER);
		return new LinkWrapper(dir, link, dest, reverse, policy);
	}
	
	/**
	 * Loads a link descriptor.
	 */
	private Link loadLink(TextNode node, Route def, Location loc, Location dest) {
		// Load common link attributes
		final boolean isRouteLink = node.getBoolean("route", false);
		final Route route = node.getAttribute("route", isRouteLink ? def : Route.NONE, LoaderHelper.ROUTE);
		final Size size = node.getAttribute("size", Size.NONE, Size.CONVERTER);
		
		// Lookup sub-class loader
		final Loader loader = Util.getEnumConstant(node.name(), Loader.class, () -> node.exception("Invalid link type: " + node.name()));
		return loader.load(node, route, size, loc, dest, this);
	}

	/**
	 * Sub-class loaders.
	 */
	private enum Loader {
		LINK {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				return Link.DEFAULT;
			}
		},
		
		ROUTE {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				if(route == Route.NONE) throw node.exception("Illogical route-type");
				return new RouteLink(route);
			}
		},
		
		DOOR {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				// Load object descriptor
				final ObjectDescriptor descriptor = loader.objectLoader.loadDescriptor(node, "door");
				
				// Create portal descriptor
				final Openable.Lock lock = loader.objectLoader.getObjectLoader().loadLock(node, true);
				final Portal.Descriptor portal = new Portal.Descriptor(descriptor, lock);

				// Create portal
				final Portal obj = new Portal(portal, dest);

				// Register custom portals
				if(node.optionalChild().isPresent()) {
					LOG.log(Level.FINE, "Custom portal object: {0}", obj.getName());
					loader.world.getObjects().add(obj);
				}

				// Create link
				return new PortalLink(route, loader.loadScript(node, 1), size, obj);
			}
		},
		
		GATE {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				// Load object descriptor
				final ObjectDescriptor descriptor = loader.objectLoader.loadDescriptor(node, "gate");

				// Create gate
				final Portal.Descriptor portal = new Portal.Descriptor(descriptor, Openable.FIXED);
				final Gate gate = new Gate(portal, dest);
				
				// Register open/close events
				final Consumer<Boolean> toggle = open -> {
					// Reset gate
					gate.reset(open);

					// Notify locations
					if(gate.isOpen() != open) {
						final Notification n = new Message("gate." + (open ? "open" : "close"));
						loc.broadcast(Actor.SYSTEM, n);
						dest.broadcast(Actor.SYSTEM, n);
					}
				};
				final ToggleListener listener = LoaderHelper.loadToggleListener(node, toggle);
				Clock.CLOCK.add(listener);

				// Create link
				return new PortalLink(route, Script.NONE, size, gate);
			}
		},
		
		HIDDEN {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				final String name = node.getString("name", null);
				final Percentile vis = node.getAttribute("vis", null, loader.difficultyConverter);
				final long forget = node.getAttribute("forget", loader.forget, Converter.DURATION).toMillis();
				return new HiddenLink(route, loader.loadScript(node, 0), size, name, vis, forget);
			}
		},
		
		ROPE {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				if(route != Route.NONE) throw node.exception("Cannot over-ride route-type for a rope-link");
				final ObjectDescriptor descriptor = loader.objectLoader.loadDescriptor(node);
				final boolean quiet = node.getBoolean("quiet", false);
				final Rope.Anchor anchor = new Rope.Anchor(descriptor);
				return new RopeLink(anchor, quiet);
			}
		},
		
		OBJECT {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				final WorldObject obj = loader.objectLoader.loadDescriptor(node).create();
				if(!obj.getOpenableModel().isPresent()) throw node.exception("Object must be openable");
				final String reason = node.getString("reason", "move.link.closed");
				return new ObjectLink(route, loader.loadScript(node, 1), size, obj, reason);
			}
		},
		
		CLIMB {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				final WorldObject obj = loader.objectLoader.loadDescriptor(node).create();
				if(!obj.isFixture()) throw node.exception("Climbable objects must be fixtures");
				return new ExtendedLink(route, loader.loadScript(node, 1), size) {
					@Override
					public boolean isVisible(Actor actor) {
						return false;
					}
					
					@Override
					public Optional<Thing> getController() {
						return Optional.of(obj);
					}
				};
			}
		},
		
		CONTAINER {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				final ObjectDescriptor descriptor = loader.objectLoader.loadDescriptor(node);
				final String name = node.getString("name", null);
				return new ContainerLink(route, loader.loadScript(node, 1), size, name, descriptor);
			}
		},
		
		SCRIPT {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				return new ExtendedLink(route, loader.loadScript(node, 0), size);
			}
		},
		
		FAKE {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				final String name = node.getString("name", null);
				final String message = node.getString("message", null);
				return new FakeLink(route, size, name, message);
			}
		},
		
		MESSAGE {
			@Override
			protected Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader) {
				final String message = node.getString("message", null);
				return new ExtendedLink(route, Script.message(message), size);
			}
		};
		
		/**
		 * Loads a link.
		 */
		protected abstract Link load(TextNode node, Route route, Size size, Location loc, Location dest, LinkLoader loader);
	}

	/**
	 * Helper - Loads a script.
	 * @param index Child index
	 */
	private Script loadScript(TextNode node, int index) {
		final ScriptLoader loader = objectLoader.getObjectLoader().getScriptLoader();
		return node.optionalChild("script").map(TextNode::child).map(loader::load).orElse(Script.NONE);
	}
}
