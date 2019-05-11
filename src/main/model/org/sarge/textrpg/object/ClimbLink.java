package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.ExtendedLink;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.SlopeLink;

/**
 * A <i>climb</i> is a link for an exit that can <b>only</b> be climbed, e.g. a tree or cliff.
 * @author Sarge
 */
public class ClimbLink extends SlopeLink {
	private final Optional<Thing> obj;
	private final boolean quiet;
	private final Percentile diff;

	/**
	 * Constructor.
	 * @param props			Properties
	 * @param up			Whether climbing up or down
	 * @param obj			Climbable object
	 * @param quiet			Whether this is link is quiet
	 * @param diff			Climb difficulty
	 */
	public ClimbLink(ExtendedLink.Properties props, boolean up, WorldObject obj, boolean quiet, Percentile diff) {
		super(props, up);
		if(!obj.descriptor().isFixture()) throw new IllegalArgumentException("Climb object must be a fixture");
		this.obj = Optional.of(obj);
		this.quiet = quiet;
		this.diff = notNull(diff);
	}

	/**
	 * @return Climb difficulty
	 */
	public Percentile difficulty() {
		return diff;
	}

	@Override
	public Optional<Description> reason(Thing actor) {
		return Optional.of(Description.of(Link.INVALID_DIRECTION));
	}

	@Override
	public boolean isQuiet() {
		return quiet;
	}

	@Override
	public boolean isEntityOnly() {
		return true;
	}

	@Override
	public boolean isTraversable() {
		return false;
	}

	@Override
	public Optional<Thing> controller() {
		return obj;
	}

	@Override
	public Link invert() {
		return new ClimbLink(super.properties(), !super.up(), (WorldObject) obj.get(), quiet, diff);
	}
}
