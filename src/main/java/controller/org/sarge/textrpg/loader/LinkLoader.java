package org.sarge.textrpg.loader;

import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.ConverterAdapter;
import org.sarge.lib.util.Util;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.object.*;
import org.sarge.textrpg.util.Percentile;
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
	 * @param node		Link descriptor
	 * @param route		Default route-type
	 * @param loc		Start location
	 * @return Link-wrapper
	 */
	public LinkWrapper loadLinkWrapper(Element node, Route route, Location loc) {
		// Lookup destination
		final ConverterAdapter attrs = node.attributes();
		final String name = attrs.toString("dest", null);
		final boolean self = name.equals("self");
		final Location dest;
		if(self) {
			dest = loc;
		}
		else {
			dest = world.getLocations().find(name);
			if(dest == null) throw node.exception("Unknown destination: " + name);
		}
		
		// Init transient link context
		final LinkContext ctx = new LinkContext();
		ctx.loc = loc;
		ctx.dest = dest;
		ctx.loader = this;
		
		// Create link
		final Direction dir = attrs.toValue("dir", null, Direction.CONVERTER);
		final Link link = loadLink(node, route, ctx);
		
		// Wrap link
		final ReversePolicy def = self ? ReversePolicy.ONE_WAY : ReversePolicy.INVERSE;
		final ReversePolicy policy = attrs.toValue("reverse", def, REVERSE);
		final Direction reverse = attrs.toValue("reverse-dir", dir.reverse(), Direction.CONVERTER);
		return new LinkWrapper(dir, link, dest, reverse, policy);
	}
	
	/**
	 * Loads a link descriptor.
	 */
	private static Link loadLink(Element node, Route def, LinkContext ctx) {
		// Load common link attributes
		final ConverterAdapter attrs = node.attributes();
		final boolean isRouteLink = attrs.toBoolean("route", false);
		ctx.route = attrs.toValue("route", isRouteLink ? def : Route.NONE, LoaderHelper.ROUTE);
		ctx.size = attrs.toValue("size", Size.NONE, Size.CONVERTER);
		
		// Delegate to sub-class loader
		final Loader loader = Util.getEnumConstant(node.name(), Loader.class, () -> node.exception("Invalid link type: " + node.name()));
		return loader.load(node, ctx);
	}
	
	private class LinkContext {
		private Route route;
		private Size size;
		private Location loc, dest;
		private LinkLoader loader;
	}

	/**
	 * Sub-class loaders.
	 */
	private enum Loader {
		LINK {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				return Link.DEFAULT;
			}
		},
		
		ROUTE {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				if(ctx.route == Route.NONE) throw node.exception("Illogical route-type");
				return new RouteLink(ctx.route);
			}
		},
		
		DOOR {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				// Load object descriptor
				final ObjectDescriptor descriptor = ctx.loader.objectLoader.loadDescriptor(node, "door");
				
				// Create portal descriptor
				final Openable.Lock lock = ctx.loader.objectLoader.getObjectLoader().loadLock(node, true);
				final Portal.Descriptor portal = new Portal.Descriptor(descriptor, lock);

				// Create portal
				final Portal obj = new Portal(portal, ctx.dest);

				// Register custom portals
				if(node.optionalChild().isPresent()) {
					LOG.log(Level.FINE, "Custom portal object: {0}", obj.getName());
					ctx.loader.world.getObjects().add(obj);
				}

				// Create link
				return new PortalLink(ctx.route, ctx.loader.loadScript(node, 1), ctx.size, obj);
			}
		},
		
		GATE {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				// Load object descriptor
				final ObjectDescriptor descriptor = ctx.loader.objectLoader.loadDescriptor(node, "gate");

				// Create gate
				final Portal.Descriptor portal = new Portal.Descriptor(descriptor, Openable.FIXED);
				final Gate gate = new Gate(portal, ctx.dest);
				
				// TODO - this should be in the Gate class itself?
				/*
				// Register open/close events
				final Consumer<Boolean> toggle = open -> {
					// Reset gate
					gate.reset(open);

					// Notify locations
					if(gate.isOpen() != open) {
						final Notification n = new Message("gate." + (open ? "open" : "close"));
						ctx.loc.broadcast(Actor.SYSTEM, n);
						ctx.dest.broadcast(Actor.SYSTEM, n);
					}
				};
				final ToggleListener listener = LoaderHelper.loadToggleListener(node, toggle);
				Clock.CLOCK.add(listener);
				*/

				// Create link
				return new PortalLink(ctx.route, Script.NONE, ctx.size, gate);
			}
		},
		
		HIDDEN {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				final ConverterAdapter attrs = node.attributes();
				final String name = attrs.toString("name", null);
				final Percentile vis = attrs.toValue("vis", null, ctx.loader.difficultyConverter);
				final long forget = attrs.toValue("forget", ctx.loader.forget, Converter.DURATION).toMillis();
				return new HiddenLink(ctx.route, ctx.loader.loadScript(node, 0), ctx.size, name, vis, forget);
			}
		},
		
		ROPE {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				if(ctx.route != Route.NONE) throw node.exception("Cannot over-ride route-type for a rope-link");
				final ObjectDescriptor descriptor = ctx.loader.objectLoader.loadDescriptor(node);
				final boolean quiet = node.attributes().toBoolean("quiet", false);
				final Rope.Anchor anchor = new Rope.Anchor(descriptor);
				return new RopeLink(anchor, quiet);
			}
		},
		
		OBJECT {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				final WorldObject obj = ctx.loader.objectLoader.loadDescriptor(node).create();
				if(!obj.getOpenableModel().isPresent()) throw node.exception("Object must be openable");
				final String reason = node.attributes().toString("reason", "move.link.closed");
				return new ObjectLink(ctx.route, ctx.loader.loadScript(node, 1), ctx.size, obj, reason);
			}
		},
		
		CLIMB {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				final WorldObject obj = ctx.loader.objectLoader.loadDescriptor(node).create();
				if(!obj.isFixture()) throw node.exception("Climbable objects must be fixtures");
				return new ExtendedLink(ctx.route, ctx.loader.loadScript(node, 1), ctx.size) {
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
			protected Link load(Element node, LinkContext ctx) {
				final ObjectDescriptor descriptor = ctx.loader.objectLoader.loadDescriptor(node);
				final String name = node.attributes().toString("name", null);
				return new ContainerLink(ctx.route, ctx.loader.loadScript(node, 1), ctx.size, name, descriptor);
			}
		},
		
		SCRIPT {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				return new ExtendedLink(ctx.route, ctx.loader.loadScript(node, 0), ctx.size);
			}
		},
		
		FAKE {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				final String name = node.attributes().toString("name", null);
				final String message = node.attributes().toString("message", null);
				return new FakeLink(ctx.route, ctx.size, name, message);
			}
		},
		
		MESSAGE {
			@Override
			protected Link load(Element node, LinkContext ctx) {
				final String message = node.attributes().toString("message", null);
				return new ExtendedLink(ctx.route, Script.message(message), ctx.size);
			}
		};
		
		/**
		 * Loads a link.
		 */
		protected abstract Link load(Element node, LinkContext ctx);
	}

	/**
	 * Helper - Loads a script.
	 * @param index Child index
	 */
	private Script loadScript(Element node, int index) {
		final ScriptLoader loader = objectLoader.getObjectLoader().getScriptLoader();
		return node.optionalChild("script").map(Element::child).map(loader::load).orElse(Script.NONE);
	}
}
