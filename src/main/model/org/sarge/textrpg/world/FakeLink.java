package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.Optional;

import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;

/**
 * Link to a destination that cannot be reached.
 * @author Sarge
 */
public class FakeLink extends Link {
	private final String name;
	private final Optional<Description> reason;

	/**
	 * Constructor.
	 * @param name			Fake destination name
	 * @param reason		Reason code
	 */
	public FakeLink(String name, String reason) {
		this.name = notEmpty(name);
		this.reason = Optional.of(new Description(reason));
	}

	@Override
	public String name(Location dest) {
		return name;
	}

	@Override
	public boolean isTraversable() {
		return false;
	}

	@Override
	public Optional<Description> reason(Thing actor) {
		return reason;
	}

	@Override
	public Link invert() {
		throw new UnsupportedOperationException("Cannot invert a fake link");
	}
}
