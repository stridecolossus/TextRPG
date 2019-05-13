package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Map;

import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.ArgumentFormatter.Registry;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;

/**
 * An <i>intangible</i> object can be examined but not interacted with, e.g. a sound, vapour, a beam of light, etc.
 * @author Sarge
 */
public class Intangible extends Thing {
	private final String name;
	private final Percentile vis;
	private final Map<Emission, Percentile> emissions;

	/**
	 * Constructor.
	 * @param name			Name of this intangible object
	 * @param vis			Visibility
	 * @param emissions		Emission(s) generated by this intangible
	 */
	public Intangible(String name, Percentile vis, Map<Emission, Percentile> emissions) {
		this.name = notEmpty(name);
		this.vis = notNull(vis);
		this.emissions = Map.copyOf(emissions);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Percentile visibility() {
		return vis;
	}

	@Override
	public Percentile emission(Emission emission) {
		return emissions.getOrDefault(emission, Percentile.ZERO);
	}

	@Override
	public Description describe(Registry formatters) {
		return new Description("intangible.object", name);
	}
}