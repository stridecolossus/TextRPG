package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.ExtendedLink;

/**
 * A <i>blocked</i> link contains an object that must be destroyed to traverse the link.
 * @author Sarge
 */
public class BlockedLink extends ExtendedLink {
	private final WorldObject blockage;
	private final Optional<Description> reason;

	/**
	 * Constructor.
	 * @param props			Properties
	 * @param blockage		Blockage
	 */
	public BlockedLink(ExtendedLink.Properties props, WorldObject blockage) {
		super(props);
		this.blockage = notNull(blockage);
		this.reason = Optional.of(new Description("link.blocked", blockage.name()));
	}

	@Override
	public Optional<Thing> controller() {
		if(blockage.isAlive()) {
			return Optional.of(blockage);
		}
		else {
			return EMPTY_CONTROLLER;
		}
	}

	@Override
	public Optional<Description> reason(Thing actor) {
		if(blockage.isAlive()) {
			return reason;
		}
		else {
			return super.reason(actor);
		}
	}

	@Override
	public String wrap(String dir) {
		final String wrapped = super.wrap(dir);
		if(blockage.isAlive()) {
			return StringUtils.wrap(wrapped, '#');
		}
		else {
			return wrapped;
		}
	}
}
