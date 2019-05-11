package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextHelper;

/**
 * Basic object that can be examined, taken, etc.
 * @author Sarge
 */
public class WorldObject extends Thing {
	/**
	 * Notification key for a decayed object.
	 * @see #decay()
	 */
	public static final String DECAYED = "object.decayed";

	/**
	 * Description argument name for the object <i>state</i>.
	 */
	public static final String KEY_STATE = "state";

	/**
	 * Description argument name for the object <i>condition</i>.
	 */
	public static final String KEY_CONDITION = "condition";

	/**
	 * Object filter.
	 */
	public interface Filter extends Predicate<WorldObject> {
		/**
		 * Creates an object filter from an object-descriptor filter.
		 * @param filter Filter
		 * @return Object filter
		 */
		static Filter of(ObjectDescriptor.Filter filter) {
			return obj -> filter.test(obj.descriptor());
		}

		/**
		 * Creates an object category filter.
		 * @param cat Category
		 * @return Category filter
		 * @see WorldObject#isCategory(String)
		 */
		static Filter of(String cat) {
			return obj -> obj.isCategory(cat);
		}
	}

	/**
	 * Types of interaction with this object.
	 */
	public enum Interaction {
		EXAMINE,
		PUSH,
		PULL,
		MOVE,
		TURN,
		PRESS;

		/**
		 * @return Inverse of this interaction
		 * @throws IllegalStateException if this interaction cannot be inverted
		 */
		public Interaction invert() {
			switch(this) {
			case EXAMINE:	throw new IllegalStateException("Cannot invert: " + this);
			case PUSH:		return PULL;
			case PULL:		return PUSH;
			default:		return this;
			}
		}
	}

	private final ObjectDescriptor descriptor;

	/**
	 * Constructor.
	 * @param descriptor Descriptor for this object
	 */
	protected WorldObject(ObjectDescriptor descriptor) {
		this.descriptor = notNull(descriptor);
	}

	@Override
	public String name() {
		return descriptor.name();
	}

	/**
	 * @return Descriptor for this object
	 */
	public ObjectDescriptor descriptor() {
		return descriptor;
	}

	@Override
	public Percentile visibility() {
		return descriptor.characteristics().visibility();
	}

	@Override
	public int weight() {
		return descriptor.properties().weight();
	}

	@Override
	public Size size() {
		return descriptor.properties().size();
	}

	/**
	 * @return Number of objects in this stack (default is <tt>one</tt>)
	 */
	public int count() {
		return 1;
	}

	/**
	 * @return Value of this object
	 * @see ObjectDescriptor.Properties#value()
	 */
	public int value() {
		return descriptor.properties().value();
	}

	@Override
	public Percentile emission(Emission emission) {
		return Percentile.ZERO;
	}

	/**
	 * @param cat Category
	 * @return Whether this object has the given category
	 * @see ObjectDescriptor.Characteristics#categories()
	 */
	public boolean isCategory(String cat) {
		return descriptor.characteristics().categories().stream().anyMatch(cat::equals);
	}

	@Override
	public boolean isQuiet() {
		return descriptor.characteristics().isQuiet();
	}

	/**
	 * @return Whether this object is being carried by an entity
	 */
	public boolean isCarried() {
		Parent parent = this.parent();
		while(true) {
			if((parent == null) || (parent == Parent.LIMBO)) {
				return false;
			}
			if(parent.isSentient()) {
				return true;
			}
			parent = parent.parent();
		}
	}

	@Override
	public final Description describe(ArgumentFormatter.Registry formatters) {
		return describe(false, formatters);
	}

	/**
	 * Determines the description key for this object.
	 * @param carried Whether this object is being carried
	 * @return Description key suffix for this object
	 */
	protected String key(boolean carried) {
		if(carried) {
			return "carried";
		}
		else
		if(parent() instanceof Container) {
			return "contained";
		}
		else {
			return "dropped";
		}
	}

	/**
	 * Describes this object.
	 * @param carried Whether this object is being carried by the actor
	 * @return Description
	 */
	public Description describe(boolean carried, ArgumentFormatter.Registry formatters) {
		// Determine description key
		final String key = key(carried);
		final var builder = new Description.Builder(TextHelper.join("object", key));

		// Add general properties
		builder.name(this.name());
		builder.add("cardinality", TextHelper.prefix(descriptor.characteristics().cardinality()));
		if(!carried) {
			builder.add("placement", TextHelper.join("placement", descriptor.characteristics().placement()));
		}

		// Add optional size
		final Size size = this.size();
		if(size == Size.NONE) {
			builder.add("size", StringUtils.EMPTY, ArgumentFormatter.PLAIN);
		}
		else {
			builder.add("size", TextHelper.prefix(size));
		}

		// Delegate to add sub-class properties
		describe(carried, builder, formatters);

		// Ensure optional arguments are defaulted to empty if not populated
		ensure(KEY_STATE, builder);
		ensure(KEY_CONDITION, builder);

		// Build description
		return builder.build();
	}

	/**
	 * Over-ridden in sub-classes to append description entries for this object.
	 * @param carried 			Whether this object is being carried by the actor
	 * @param builder 			Description builder
	 * @param formatters		Argument formatters
	 */
	protected void describe(boolean carried, Description.Builder builder, ArgumentFormatter.Registry formatters) {
		// Does nowt
	}

	/**
	 * Sets an optional description argument to empty if not populated by the sub-class description.
	 * @param arg			Argument name
	 * @param builder		Builder
	 */
	private static void ensure(String arg, Description.Builder builder) {
		if(builder.get(arg) == null) {
			builder.add(arg, StringUtils.EMPTY);
		}
	}

	/**
	 * Uses this object (default does nothing).
	 */
	public void use() {
		// Does nowt
	}

	/**
	 * @return Whether this object is damaged (default is <tt>false</tt>)
	 */
	public boolean isDamaged() {
		return false;
	}

	/**
	 * @return Whether this object is broken (default is <tt>false</tt)
	 */
	public boolean isBroken() {
		return false;
	}

	@Override
	protected void damage(Damage.Type type, int amount) {
		final Material mat = descriptor.characteristics().material();
		if(mat.isDamagedBy(type) && (amount >= mat.strength())) {
			destroy();
		}
	}

	/**
	 * Decays this object.
	 * @return Whether to repeat the decay event
	 * @see ObjectDescriptor#isPerishable()
	 */
	protected boolean decay() {
		if(descriptor.isPerishable() || !isCarried()) {
			if(weight() > 0) {
				final Description description = new Description("object.decayed", name());
				raise(ContentStateChange.of(ContentStateChange.Type.CONTENTS, description));
			}
			destroy();
		}
		return false;
	}

	@Override
	protected void destroy() {
		assert !descriptor.isFixture() || (descriptor.properties().reset() != Duration.ZERO) : "Cannot destroy a non-resetable fixture";
		super.destroy();
	}
}
