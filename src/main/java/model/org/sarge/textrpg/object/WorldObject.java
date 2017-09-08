package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.util.Percentile;

/**
 * Object that can be manipulated.
 * @author Sarge
 */
public class WorldObject extends Thing {
	/**
	 * Reason code for taking a fixture.
	 */
	protected static final ActionException FIXTURE = new ActionException("take.immovable.object");

	/**
	 * Synthetic previous or <i>it</i> object.
	 */
	public static final String PREVIOUS = "object.previous";

	/**
	 * Default openable model.
	 */
	private static final Optional<Openable> EMPTY = Optional.empty();

	/**
	 * Object interactions.
	 */
	public static enum Interaction {
		EXAMINE,
		PUSH,
		PULL,
		TURN,
		MOVE;

		/**
		 * @return Inverse of this interaction
		 */
		public Interaction invert() {
			switch(this) {
			case PUSH:		return PULL;
			case PULL:		return PUSH;
			case EXAMINE:	throw new UnsupportedOperationException();
			default:		return this;
			}
		}
	}

	protected final ObjectDescriptor descriptor;

	/**
	 * Constructor.
	 * @param descriptor Descriptor for this object
	 */
	public WorldObject(ObjectDescriptor descriptor) {
		Check.notNull(descriptor);
		this.descriptor = descriptor;
	}

	@Override
	public String name() {
		return descriptor.getName();
	}

	@Override
	public long forgetPeriod() {
		return descriptor.getProperties().getForgetPeriod();
	}

	/**
	 * @return Descriptor for this object
	 */
	public ObjectDescriptor descriptor() {
		return descriptor;
	}

	@Override
	public Percentile visibility() {
		return descriptor.getCharacteristics().getVisibility();
	}

	@Override
	public int weight() {
		return descriptor.getProperties().getWeight();
	}

	@Override
	public Size size() {
		return descriptor.getProperties().getSize();
	}

	@Override
    public boolean isQuiet() {
        return descriptor.getCharacteristics().isQuiet();
    }

	/**
	 * @return Whether this is a fixture.
	 * @see ObjectDescriptor#isFixture()
	 */
	public boolean isFixture() {
		return descriptor.isFixture();
	}

	/**
	 * @return Value of this object
	 * @see ObjectDescriptor.Properties#getValue()
	 */
	public int value() {
		return descriptor.getProperties().getValue();
	}

	@Override
	public Optional<Emission> emission(Emission.Type type) {
		return descriptor.getCharacteristics().getEmission(type);
	}

	/**
	 * @return Descriptor for this object if it can be opened (default is empty)
	 */
	public Optional<Openable> openableModel() {
		return EMPTY;
	}

	/**
	 * Over-ride in sub-classes to customise the long description
	 * @return Full description key
	 */
	protected String getFullDescriptionKey() {
		return descriptor.getCharacteristics().getFullDescriptionKey();
	}

	@Override
	public final Description describe() {
		return describe(getFullDescriptionKey()).build();
	}

	/**
	 * Generates a short description of this object.
	 * @return Short description
	 */
	public final Description describeShort() {
		return describe(descriptor.getDescriptionKey()).build();
	}

	/**
	 * Builds a description of this object.
	 * @param key Description key
	 * @return Description
	 */
	private final Description.Builder describe(String key) {
		// Start description
		final Description.Builder builder = new Description.Builder("description." + key);
		builder.wrap("name", name());

		// Add cardinality
		final Cardinality cardinality = descriptor.getCharacteristics().getCardinality();
		cardinality.add(builder);
		// TODO
		// - wrap -> helper
		// - A or AN -> SINGLE and name starts with vowel, OR another cardinality type?

		// Add optional size
		final Size size = descriptor.getProperties().getSize();
		if(size != Size.NONE) {
			builder.wrap("size", "size", size.name());
		}

		// Delegate
		describe(builder);
		return builder;
	}

	/**
	 * Over-ridden in sub-classes to append description entries for this object.
	 * @param builder Description builder
	 */
	protected void describe(Description.Builder builder) {
		// Does nowt
	}

	/**
	 * Checks whether this object can be carried by the given actor.
	 * @param actor Actor
	 * @throws ActionException if this object cannot be carried
	 */
	protected void take(Actor actor) throws ActionException {
		// Check for fixture or intangible objects
		if(isFixture()) throw FIXTURE;

		// Check not already carried
		final Parent owner = this.owner();
		if(owner == actor) throw new ActionException("take.already.carried");
		if(owner != null) throw new ActionException("take.cannot.take");
	}

	/**
	 * @return Whether this object is damaged (default is <tt>false</tt>)
	 * @see #use()
	 */
	public boolean isDamaged() {
		return false;
	}

	@Override
	protected void damage(DamageType type, int amount) {
		if(isFixture()) return;
		final Material mat = descriptor.getCharacteristics().getMaterial();
		if(mat.isDamagedBy(type) && (amount > mat.strength())) {
			destroy();
		}
	}

    /**
     * @return Whether this object is broken (default is <tt>false</tt>)
     */
    public boolean isBroken() {
        return false;
    }

    /**
     * Uses this object.
     * @throws UnsupportedOperationException by default
     */
    public void use() {
        throw new UnsupportedOperationException();
    }

	@Override
	public boolean isSentient() {
		return false;
	}

	/**
	 * Helper.
	 * @return Owner of this object or <tt>null</tt> if none
	 */
	public final Parent owner() {
		return super.path().filter(p -> p instanceof Actor).findFirst().orElse(null);
	}

	/**
	 * Sends an alert to the owner of this object (if any).
	 * @param message Message
	 */
	protected void alertOwner(String message) {
		// Ignore if destroyed
		if(isDead()) return;

		// Check for owner
		final Actor owner = (Actor) owner();
		if(owner == null) return;

		// Send alert
		owner.alert(new Message(message, this));
	}

	/**
	 * Moves this object to {@link LIMBO}.
	 */
	protected void hide() {
		super.move(LIMBO);
	}

	@Override
	public void destroy() {
		assert !isFixture();
		super.destroy();
	}
}
