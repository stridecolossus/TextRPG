package org.sarge.textrpg.world;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.util.Percentile;

/**
 * Link with an associated script and size constraint.
 * @author Sarge
 */
public class ExtendedLink extends RouteLink {
	private final Script script;
	private final Size size;

	/**
	 * Constructor.
	 * @param route			Route-type
	 * @param script		Script
	 * @param size			Maximum size to traverse this link
	 */
	public ExtendedLink(Route route, Script script, Size size) {
		super(route);
		Check.notNull(script);
		Check.notNull(size);
		this.script = script;
		this.size = size;
	}

	@Override
	public Size size() {
		return size;
	}

	@Override
	public String reason(Actor actor) {
	    if((size != Size.NONE) && actor.size().isLargerThan(size)) {
	        return "move.link.constraint";
	    }
	    else {
	        return super.reason(actor);
	    }
	}

	@Override
	public Script script() {
		return script;
	}

    /**
     * Helper - Creates a link controller proxy.
     * @param name      Name
     * @param vis       Visibility
     * @param quiet     Whether controller is quiet
     * @param forget    Forget duration (ms)
     * @return Link controller
     */
    protected static Thing createController(String name, Percentile vis, boolean quiet, long forget) {
        Check.notEmpty(name);
        Check.notNull(vis);
        Check.zeroOrMore(forget);

        return new Thing() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Percentile visibility() {
                return vis;
            }

            @Override
            public boolean isQuiet() {
                return quiet;
            }

            @Override
            public long forgetPeriod() {
                return forget;
            }

            @Override
            public int weight() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Size size() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Description describe() {
                return new Description("description.dropped", "name", name); // TODO - description key
            }
        };
    }
}
