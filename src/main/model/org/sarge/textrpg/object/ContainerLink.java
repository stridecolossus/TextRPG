package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.ExtendedLink;

/**
 * Link that requires an object to traverse, e.g. a ladder for a trap-door, or a plank for a chasm
 * @author Sarge
 */
public class ContainerLink extends ExtendedLink {
	private static final Optional<Description> EMPTY = Optional.of(new Description("link.requires.object"));

	/**
	 * Contents of this link.
	 */
	private final Contents contents = new Contents() {
		@Override
		public Optional<String> reason(Thing thing) {
			if(!isEmpty()) {
				return Optional.of("link.not.empty");
			}
			else {
				final WorldObject obj = (WorldObject) thing;
				if(obj.isCategory(cat)) {
					return EMPTY_REASON;
				}
				else {
					return Optional.of("link.invalid.object");
				}
			}
		}
	};

	/**
	 * Controller for this link.
	 */
	private class Controller extends Thing implements Parent {
		@Override
		public String name() {
			return name;
		}

		@Override
		public Parent parent() {
			return null;
		}

		@Override
		public Contents contents() {
			return contents;
		}

		@Override
		public Percentile visibility() {
			return Percentile.ONE;
		}
	}

	private final Optional<Thing> controller = Optional.of(new Controller());
	private final String name;
	private final String cat;

	/**
	 * Constructor.
	 * @param props			Properties
	 * @param name			Link name
	 * @param cat			Category of the object required to traverse this link
	 */
	public ContainerLink(ExtendedLink.Properties props, String name, String cat) {
		super(props);
		this.name = notEmpty(name);
		this.cat = notEmpty(cat);
	}

	@Override
	public Optional<Thing> controller() {
		return controller;
	}

	@Override
	public boolean isEntityOnly() {
		return true;
	}

	@Override
	public Optional<Description> reason(Thing actor) {
		if(contents.isEmpty()) {
			return EMPTY;
		}
		else {
			return super.reason(actor);
		}
	}

	@Override
	public String wrap(String dir) {
		final String wrapped = super.wrap(dir);
		if(contents.size() == 0) {
			return StringUtils.wrap(wrapped, "!");
		}
		else {
			return wrapped;
		}
	}
}
