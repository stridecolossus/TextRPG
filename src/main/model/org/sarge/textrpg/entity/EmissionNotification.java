package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Direction;

/**
 * Notification of an emission in a nearby location, e.g. smoke or noise
 * @author Sarge
 * @see Emission
 */
public final class EmissionNotification extends Notification {
	private static final String[] INTENSITY = {"low", "medium", "high"};

	private final Percentile intensity;
	private final Direction dir;

	/**
	 * Constructor.
	 * @param emission		Type of emission
	 * @param intensity		Emission intensity
	 */
	public EmissionNotification(Emission emission, Percentile intensity) {
		this(emission.name(), intensity);
	}

	/**
	 * Constructor for a sound emission with a specific name.
	 * @param name			Name
	 * @param intensity		Sound intensity
	 */
	public EmissionNotification(String name, Percentile intensity) {
		this(TextHelper.join("notification.emission", name), intensity, null);
	}

	/**
	 * Constructor.
	 * @param emission		Type of emission
	 * @param intensity		Emission intensity
	 * @param dir			Relative direction
	 */
	private EmissionNotification(String key, Percentile intensity, Direction dir) {
		super(key, null);
		this.intensity = notNull(intensity);
		this.dir = dir;
	}

	@Override
	public void handle(Handler handler, Entity entity) {
		handler.handle(this, entity);
	}

	/**
	 * @return Emission intensity
	 */
	public Percentile intensity() {
		return intensity;
	}

	@Override
	protected void describe(Description.Builder builder) {
		// Add intensity level
		final int index = (int) (intensity.floatValue() * (INTENSITY.length - 1));
		builder.add("emission.intensity", TextHelper.join("emission.intensity", INTENSITY[index]));

		// Add relative direction
		if(dir != null) {
			builder.add("dir", dir);
		}
	}

	/**
	 * Scales this notification by the link traversal depth.
	 * @param scale		Intensity scale per link traversal
	 * @param depth		Depth
	 * @param dir		Relative direction
	 * @return Scaled emission
	 */
	public EmissionNotification scale(Percentile scale, int depth, Direction dir) {
		final float scalar = (float) Math.pow(scale.floatValue(), depth);
		final Percentile scaled = intensity.scale(new Percentile(scalar));
		return new EmissionNotification(this.key(), scaled, dir.reverse());
	}
}
