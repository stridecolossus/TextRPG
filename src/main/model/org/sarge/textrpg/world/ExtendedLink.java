package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Optional;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;

/**
 * Extended link with a size constraint, optional traversal message and a custom movement cost modifier.
 * @author Sarge
 */
public class ExtendedLink extends Link {
	/**
	 * Default link properties.
	 */
	public static final Properties DEFAULT_PROPERTIES = new Properties();

	/**
	 * Size constraint reason.
	 */
	private static final Optional<Description> TOO_SMALL = Optional.of(new Description("link.too.small"));

	/**
	 * Extended link properties.
	 */
	public static class Properties extends AbstractEqualsObject {
		private final Size size;
		private final Route route;
		private final float mod;
		private final Optional<Description> message;

		/**
		 * Constructor.
		 * @param size			Size constraint
		 * @param route			Route type
		 * @param mod			Cost modifier
		 * @param message		Optional traversal message
		 */
		public Properties(Size size, Route route, float mod, String message) {
			this.size = notNull(size);
			this.route = notNull(route);
			this.mod = oneOrMore(mod);
			this.message = Optional.ofNullable(message).map(Description::of);
		}

		/**
		 * Default constructor.
		 */
		public Properties() {
			this(Size.NONE, Route.NONE, 1, null);
		}
	}

	/**
	 * Creates an extended link.
	 * @param props Link properties
	 * @return Extended link
	 * @throws IllegalArgumentException if the properties are empty
	 */
	public static ExtendedLink of(Properties props) {
		if(DEFAULT_PROPERTIES.equals(props)) throw new IllegalArgumentException("Empty properties");
		return new ExtendedLink(props);
	}

	private final Properties props;

	/**
	 * Sub-class constructor.
	 * @param route		Route-type
	 * @param size		Size constraint
	 */
	protected ExtendedLink(Properties props) {
		this.props = notNull(props);
	}

	/**
	 * @return Extended link properties
	 */
	protected Properties properties() {
		return props;
	}

	@Override
	public final Size size() {
		return props.size;
	}

	@Override
	public Route route() {
		return props.route;
	}

	@Override
	public Optional<Description> reason(Thing actor) {
		if((props.size != Size.NONE) && props.size.isLessThan(actor.size())) {
			return TOO_SMALL;
		}
		else {
			return super.reason(actor);
		}
	}

	@Override
	public float modifier() {
		return props.mod;
	}

	@Override
	public Optional<Description> message() {
		return props.message;
	}
}
