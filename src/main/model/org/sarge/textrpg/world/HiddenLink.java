package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.Optional;

import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Percentile;

/**
 * Partially visible link that can also be revealed by name.
 * @author Sarge
 */
public class HiddenLink extends ExtendedLink implements CommandArgument {
	private final String name;
	private final Optional<Thing> controller;

	/**
	 * Constructor.
	 * @param props		Properties
	 * @param name		Name of this hidden link
	 * @param vis		Visibility of the link
	 */
	public HiddenLink(ExtendedLink.Properties props, String name, Percentile vis) {
		super(props);
		this.name = notEmpty(name);
		this.controller = Optional.of(create(name, vis));
	}

	/**
	 * Creates a proxy controller object for this link.
	 */
	private static Thing create(String name, Percentile vis) {
		return new Thing() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public Percentile visibility() {
				return vis;
			}
		};
	}

	/**
	 * @return Name of this hidden link
	 */
	@Override
	public String name() {
		return name;
	}

	@Override
	public Optional<Thing> controller() {
		return controller;
	}

	@Override
	public String key() {
		return "revealed";
	}
}
