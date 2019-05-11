package org.sarge.textrpg.object;

import java.util.HashSet;
import java.util.Set;

import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.PeriodModel;
import org.sarge.textrpg.world.TimePeriod;

/**
 * A <i>lamp</i> is a pseudo light-source that is controlled globally.
 * <p>
 * Notes:
 * <ul>
 * <li>{@link #setActive(boolean)} controls <b>all</b> lights</li>
 * <li>A lamp is <b>not</b> a {@link Light}</li>
 * </ul>
 * @author Sarge
 */
public class Lamp extends WorldObject implements PeriodModel.Listener<TimePeriod> {
	private static final Set<Lamp> LAMPS = new HashSet<>();

	private static boolean ACTIVE;

	/**
	 * Sets the state of <b>all</b> lamps.
	 * @param active Whether lamps are active
	 */
	public static void setActive(boolean active) {
		ACTIVE = active;
		LAMPS.forEach(lamp -> lamp.raise(ContentStateChange.LIGHT_MODIFIED));
	}

	/**
	 * Constructor.
	 * @param descriptor Lamp descriptor
	 */
	public Lamp(ObjectDescriptor descriptor) {
		super(descriptor);
		if(!descriptor.isFixture()) throw new IllegalArgumentException("Lamps must be fixtures");
		LAMPS.add(this);
	}

	@Override
	public Percentile emission(Emission emission) {
		if(ACTIVE && (emission == Emission.LIGHT)) {
			return Percentile.ONE;
		}
		else {
			return Percentile.ZERO;
		}
	}

	@Override
	protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
		if(ACTIVE) {
			builder.add(KEY_STATE, "light.active");
		}
		super.describe(carried, builder, formatters);
	}

	@Override
	public void update(TimePeriod period) {
		// TODO - replace with dawn/dusk listener
		if(period.name().equals("dawn")) {
			Lamp.setActive(true);
		}
		else
		if(period.name().equals("dusk")) {
			Lamp.setActive(false);
		}
	}
}
