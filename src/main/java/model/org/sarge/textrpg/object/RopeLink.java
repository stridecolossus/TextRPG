package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StringUtil;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.Route;

/**
 * Link that requires a {@link Rope} to be traversed.
 * TODO
 * - extended link so can have size constraint?
 * @author Sarge
 */
public class RopeLink extends Link {
	private final Rope.Anchor anchor;
	private final boolean quiet;

	/**
	 * Constructor.
	 * @param anchor	Rope anchor
	 * @param quiet		Whether this link is listed when nothing is attached
	 */
	public RopeLink(Rope.Anchor anchor, boolean quiet) {
		Check.notNull(anchor);
		this.anchor = anchor;
		this.quiet = quiet;
	}

	@Override
	public Route getRoute() {
		return Route.ROPE;
	}

	@Override
	public Optional<Thing> getController() {
		return Optional.of(anchor);
	}

	@Override
	public boolean isVisible(Actor actor) {
		if(quiet && !anchor.isAttached()) {
			return false;
		}
		else {
			return super.isVisible(actor);
		}
	}

	@Override
	public String reason(Actor actor) {
        if(anchor.isAttached()) {
            return super.reason(actor);
        }
        else {
            return "rope.not.attached";
        }
	}

	@Override
	public String describe(String dir) {
		final String icon = anchor.isAttached() ? "|" : "!";
		return StringUtil.wrap(dir, icon);
	}

	@Override
	public Description.Builder describe() {
		final Rope rope = anchor.getRope();
		if(rope == null) {
			return super.describe();
		}
		else {
			final Description.Builder builder = new Description.Builder("exit.portal");
			final Cardinality cardinality = rope.getDescriptor().getCharacteristics().getCardinality();
			cardinality.add(builder);
			builder.wrap("name", rope.getName());
			return builder;
		}
	}
}
