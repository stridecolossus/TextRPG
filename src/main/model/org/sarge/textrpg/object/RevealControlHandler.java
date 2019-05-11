package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.function.Supplier;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.util.Description;

/**
 * Control handler that reveals a hidden object.
 * @author Sarge
 */
public abstract class RevealControlHandler extends AbstractEqualsObject implements Control.Handler {
	/**
	 * Creates a handler that reveals a hidden fixture.
	 * @param obj Object to reveal
	 * @return Fixture reveal handler
	 */
	public static RevealControlHandler fixture(WorldObject obj) {
		if(!obj.descriptor().isFixture()) throw new IllegalArgumentException("Revealed object must be a fixture: " + obj);

		return new RevealControlHandler(() -> obj) {
			@Override
			public Description handle(Actor actor, Control control, boolean activated) {
				// Delegate
				final Description response = super.handle(actor, control, activated);

				// Register known object
				if(activated) {
					final Duration forget = obj.descriptor().properties().reset();
					// TODO - actor.hidden().add(obj, forget);
					// move this to controller somehow?
				}

				return response;
			}
		};
	}

	/**
	 * Creates a handler that generates a revealed object.
	 * @param descriptor Descriptor for the generated object
	 * @return Create-object reveal handler
	 */
	public static RevealControlHandler factory(ObjectDescriptor descriptor) {
		if(descriptor.isFixture()) throw new IllegalArgumentException("Cannot generate fixtures: " + descriptor);

		return new RevealControlHandler(descriptor::create) {
			@Override
			public Description handle(Actor actor, Control control, boolean activated) {
				// Destroy object if not taken
				if(!activated) {
					if(obj.parent() == control.parent()) {
						obj.destroy();
					}
				}

				// Delegate
				return super.handle(actor, control, activated);
			}
		};
	}

	private final Supplier<WorldObject> factory;

	protected WorldObject obj;

	/**
	 * Constructor.
	 * @param factory Factory for revealed object(s)
	 */
	protected RevealControlHandler(Supplier<WorldObject> factory) {
		this.factory = notNull(factory);
	}

	@Override
	public Description handle(Actor actor, Control control, boolean activated) {
		if(activated) {
			// Generate object and add to controls location
			assert obj == null;
			obj = factory.get();
			obj.parent(control.parent());
			return new Description("revealed.object", obj.name());
		}
		else {
			// Cleanup on reset
			assert obj != null;
			obj = null;
			return null;
		}
	}
}
