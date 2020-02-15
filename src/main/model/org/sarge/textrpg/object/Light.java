package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Optional;

import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;

/**
 * A <i>light</i> is a source of illumination such as a torch, lantern or camp-fire.
 * @author Sarge
 */
public class Light extends WorldObject {
	/**
	 * Tinderbox category.
	 */
	public static final String TINDERBOX = "tinderbox";

	/**
	 * Light sub-types.
	 */
	public enum Type {
		/**
		 * Default light.
		 */
		DEFAULT,

		/**
		 * Lantern that can be covered and re-fueled using {@link Liquid#OIL}.
		 */
		LANTERN,

		/**
		 * Camp-fire.
		 */
		CAMPFIRE,

		/**
		 * Light that cannot be extinguished.
		 */
		PERMANENT,
	}

	/**
	 * Light descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final Type type;
		private final long lifetime;
		private final Percentile light;
		private final Percentile smoke;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param type				Light sub-type
		 * @param lifetime			Lifetime of this light
		 * @param light				Light-level
		 * @param smoke				Smoke intensity
		 * @throws IllegalArgumentException if the lifetime is not positive
		 */
		public Descriptor(ObjectDescriptor descriptor, Type type, Duration lifetime, Percentile light, Percentile smoke) {
			super(descriptor);
			if(lifetime.isNegative()) throw new IllegalArgumentException("Lifetime must be positive");
			if((type != Type.PERMANENT) && lifetime.isZero()) throw new IllegalArgumentException("Lifetime must be one-or-more");
			this.type = notNull(type);
			this.lifetime = lifetime.toMillis();
			this.light = notNull(light);
			this.smoke = notNull(smoke);
		}

		/**
		 * @return Light sub-type
		 */
		public Type type() {
			return type;
		}

		@Override
		public Light create() {
			return new Light(this);
		}
	}

	/**
	 * Helper - Finds an active light of the given type in the actors location.
	 * @param loc		Location
	 * @param type		Light type
	 * @return Light
	 */
	public static Optional<Light> find(Parent parent, Type type) {
		return parent.contents().select(Light.class)
			.filter(Light::isActive)
			.filter(light -> light.descriptor().type() == type)
			.findAny();
	}

	// State
	private boolean active;
	private boolean covered;

	// Lifetime
	private long lifetime;
	private long started;

	// Expiry events
	private final Event.Holder expiry = new Event.Holder();
	private final Event.Holder warning = new Event.Holder();

	/**
	 * Constructor.
	 * @param descriptor Object descriptor
	 */
	protected Light(Descriptor descriptor) {
		super(descriptor);
		lifetime = descriptor.lifetime;
		if(type() == Type.PERMANENT) {
			active = true;
		}
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	/**
	 * @return Type of this light
	 */
	public Type type() {
		return descriptor().type();
	}

	/**
	 * @return Whether this light is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return Whether this lantern has been covered
	 */
	public boolean isCovered() {
		return covered;
	}

	/**
	 * @return Light lifetime (ms)
	 */
	public long lifetime() {
		return lifetime;
	}

	/**
	 * @return Expiry event
	 */
	public Event.Holder expiry() {
		return expiry;
	}

	/**
	 * @return Warning event
	 */
	public Event.Holder warning() {
		return warning;
	}

	/**
	 * @return Whether this light can be activated
	 */
	public boolean isLightable() {
		if(active) return false;
		if(lifetime == 0) return false;
		return true;
	}

	@Override
	public Percentile visibility() {
		if(active && !covered) {
			return Percentile.ONE;
		}
		else {
			return super.visibility();
		}
	}

	@Override
	public Percentile emission(Emission emission) {
		if(active && !covered) {
			final Descriptor descriptor = this.descriptor();
			switch(emission) {
			case LIGHT: return descriptor.light;
			case SMOKE: return descriptor.smoke;
			}
		}

		return super.emission(emission);
	}

	@Override
	public String key(boolean carried) {
		if(type() == Type.CAMPFIRE) {
			if(active) {
				return "camp.active";
			}
			else {
				return "camp.remains";
			}
		}
		else {
			return super.key(carried);
		}
	}

	@Override
	protected void describe(boolean carried, Builder builder) {
		// Delegate
		super.describe(carried, builder);

		// Add lit flag
		if(isActive()) {
			builder.add(KEY_STATE, "light.active");
		}

		// Add remaining lifetime
		if(carried) {
			final float ratio = lifetime / (float) descriptor().lifetime;
			builder.add("light.lifetime", new BandingArgument(new Percentile(ratio), "light.lifetime"));
		}
	}

	/**
	 * Lights this light.
	 * @param now Current time
	 * @throws ActionException if this light is already lit or has expired
	 * @see #isActive()
	 */
	void light(long now) throws ActionException {
		if(active) throw ActionException.of("light.already.active");
		if(lifetime == 0) throw ActionException.of("light.expired");
		active = true;
		started = now;
	}

	/**
	 * Snuffs this light.
	 * @param now Current time
	 * @throws ActionException if this light is not lit or is {@link Type#PERMANENT}
	 */
	void snuff(long now) throws ActionException {
		// Snuff light
		if(!active) throw ActionException.of("light.not.active");
		if(type() == Type.PERMANENT) throw ActionException.of("light.snuff.permanent");
		active = false;

		// Calculate remaining lifetime
		final long used = now - started;
		lifetime -= used;
		assert used >= 0;
		assert lifetime >= 0;
	}

	/**
	 * Covers this lantern.
	 * @throws ActionException if this light is not a lantern or is already covered
	 */
	void cover() throws ActionException {
		if(covered) throw ActionException.of("light.already.covered");
		if(type() != Type.LANTERN) throw ActionException.of("light.cannot.cover");
		covered = true;
	}

	/**
	 * Uncovers this lantern.
	 * @throws ActionException if this light is not a lantern or is not covered
	 */
	void uncover() throws ActionException {
		if(!covered) throw ActionException.of("light.not.covered");
		covered = false;
	}

	/**
	 * Expires this light.
	 * @throws IllegalStateException if this light cannot be expired
	 */
	void expire() {
		if(descriptor().type() == Type.PERMANENT) throw new IllegalStateException("Cannot expire a permanent light");
		if(!active || (lifetime <= 0)) throw new IllegalStateException("Light cannot be expired: " + this);
		active = false;
		lifetime = 0;
	}

	/**
	 * Fills this lantern from the given oil receptacle.
	 * @param rec Receptacle
	 * @throws ActionException if the light cannot be re-fueled, is already full, or the receptacle is empty or does not contain {@link Liquid#OIL}
	 */
	void fill(Receptacle rec) throws ActionException {
		// Check receptacle
		if(type() != Type.LANTERN) throw ActionException.of("light.cannot.refuel");
		if(rec.descriptor().liquid() != Liquid.OIL) throw ActionException.of("light.fill.invalid");
		if(rec.level() == 0) throw ActionException.of("light.fill.empty");

		// Check lifetime
		final long max = this.descriptor().lifetime;
		if(lifetime == max) throw ActionException.of("light.already.filled");

		// Fill from receptacle (one unit of oil = 1 minutes lifetime)
		// TODO - ratio should be parameter from controller
		final int required = (int) Duration.ofMillis(max - lifetime).toMinutes();
		final int actual = rec.consume(Math.min(1, required));
		lifetime += Duration.ofMinutes(actual).toMillis();
		assert lifetime <= max;
	}

	@Override
	protected void destroy() {
		expiry.cancel();
		warning.cancel();
		super.destroy();
	}
}
