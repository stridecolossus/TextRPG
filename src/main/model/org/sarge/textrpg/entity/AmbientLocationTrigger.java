package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.entity.Entity.LocationTrigger;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.world.AmbientEvent;
import org.sarge.textrpg.world.Location;

/**
 * Trigger for a <i>local</i> ambient event.
 * @author Sarge
 */
public class AmbientLocationTrigger extends AbstractObject implements LocationTrigger {
	private final AmbientEvent ambient;

	/**
	 * Constructor.
	 * @param ambient
	 */
	public AmbientLocationTrigger(AmbientEvent ambient) {
		this.ambient = notNull(ambient);
	}

	@Override
	public void trigger(Entity actor) {
		final Location loc = actor.location();
		final Event event = () -> {
			if(loc == actor.location()) {
				actor.alert(ambient.description());
				return ambient.isRepeating();
			}
			else {
				return false;
			}
		};
		actor.manager().queue().add(event, ambient.period());
	}
}
