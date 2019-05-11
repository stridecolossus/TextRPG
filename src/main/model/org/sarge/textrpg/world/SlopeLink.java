package org.sarge.textrpg.world;

import org.apache.commons.lang3.StringUtils;
import org.sarge.textrpg.util.Description.Builder;

/**
 * Link for a steep slope.
 * @author Sarge
 */
public class SlopeLink extends ExtendedLink {
	private final boolean up;

	/**
	 * Constructor.
	 * @param props 	Link properties
	 * @param up		Whether sloping up or down
	 */
	public SlopeLink(ExtendedLink.Properties props, boolean up) {
		super(props);
		this.up = up;
	}

	/**
	 * @return Whether slope is up or down
	 */
	public boolean up() {
		return up;
	}

	@Override
	public String wrap(String dir) {
		return StringUtils.wrap(dir, up ? "/" : "\\");
	}

	@Override
	public void describe(Builder description) {
		super.describe(description);
		description.add("slope.direction", up ? "up" : "down");
	}

	@Override
	public Link invert() {
		return new SlopeLink(super.properties(), !up);
	}
}
