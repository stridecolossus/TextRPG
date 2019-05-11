package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Function;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.PeriodModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * A <i>light-level provider</i> is used to determine the light-level in the actors location.
 * @author Sarge
 * @see AbstractAction.Flag#LIGHT
 */
// TODO - weather modifier
@Service
public class LightLevelProvider extends AbstractObject implements PeriodModel.Listener<TimePeriod> {
	/**
	 * Source of light.
	 */
	private interface LightSource {
		/**
		 * @return Whether light is available
		 */
		boolean isAvailable();

		/**
		 * Calculates the light-level for the given location.
		 * @param loc Location
		 * @return Light-level
		 */
		Percentile level(Location loc);
	}

	/**
	 * Artificial light source.
	 */
	private static class Artificial implements LightSource {
		private final Percentile level;

		private Artificial(Location loc) {
			this.level = loc.emission(Emission.LIGHT);
		}

		@Override
		public boolean isAvailable() {
			return Percentile.ZERO.isLessThan(level);
		}

		@Override
		public Percentile level(Location loc) {
			return level;
		}
	}

	/**
	 * Fully lit light source.
	 */
	private static final LightSource FULL = new LightSource() {
		@Override
		public boolean isAvailable() {
			return true;
		}

		@Override
		public Percentile level(Location loc) {
			return Percentile.ONE;
		}
	};

	/**
	 * Natural light source.
	 */
	private final LightSource natural = new LightSource() {
		@Override
		public boolean isAvailable() {
			return true;
		}

		@Override
		public Percentile level(Location loc) {
			final Percentile scale = terrain.apply(loc.terrain());
			final Percentile level = ambient.scale(scale);
			if(Percentile.ONE.equals(level)) {
				return Percentile.ONE;
			}
			else {
				return level.max(loc.emission(Emission.LIGHT));
			}
		}
	};

	private Function<Terrain, Percentile> terrain = t -> Percentile.ONE;
	private Percentile threshold = Percentile.ZERO;

	@Autowired
	@Value("${light.level.override:false}")
	private boolean override;

	private Percentile ambient = Percentile.ZERO;

	/**
	 * Sets the terrain modifier for the ambient light-level.
	 * @param terrain Terrain modifier
	 */
	@Autowired
	public void setTerrainModifier(@Value("#{terrain.function('light', T(org.sarge.textrpg.util.Percentile).CONVERTER)}") Function<Terrain, Percentile> terrain) {
		this.terrain = notNull(terrain);
	}

	/**
	 * Sets the visibility for partially visible objects and exits.
	 * @param threshold Visibility threshold
	 */
	@Autowired
	public void setVisibilityThreshold(@Value("${light.visibility.threshold}") Percentile threshold) {
		this.threshold = notNull(threshold);
	}

	@Override
	public void update(TimePeriod period) {
		this.ambient = period.light();
	}

	/**
	 * @return Whether currently daylight
	 */
	public boolean isDaylight() {
		return Percentile.ZERO.isLessThan(ambient);
	}

	/**
	 * Determines the light-source at the given location.
	 * @param loc Location
	 * @return Light source
	 */
	protected LightSource source(Location loc) {
		final Terrain terrain = loc.terrain();
		if(override || (terrain == Terrain.INDOORS)) {
			return FULL;
		}
		else
		if(!terrain.isDark() && isDaylight()) {
			return natural;
		}
		else {
			return new Artificial(loc);
		}
	}

	/**
	 * Tests whether <b>any</b> light is available in the given location.
	 * @param loc Location
	 * @return Whether light is available
	 */
	public boolean isAvailable(Location loc) {
		final LightSource level = source(loc);
		return level.isAvailable();
	}

	/**
	 * Determines the light-level in the given location.
	 * @param loc Location
	 * @return Light-level
	 */
	public Percentile level(Location loc) {
		final LightSource level = source(loc);
		return level.level(loc);
	}

	/**
	 * Tests whether the given light-level is <i>partial</i>.
	 * @param level Light-level
	 * @return Whether partial light-level
	 */
	public boolean isPartial(Percentile level) {
		return level.isLessThan(threshold);
	}
}
