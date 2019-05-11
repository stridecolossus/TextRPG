package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to smoke pipe-weed.
 * @author Sarge
 */
@Component
@RequiresActor
public class SmokeAction extends AbstractAction {
	private static final WorldObject.Filter WEED = WorldObject.Filter.of("pipe.weed");

	private final Duration duration;

	/**
	 * Constructor.
	 * @param duration Duration
	 */
	public SmokeAction(@Value("${smoke.duration}") Duration duration) {
		super(Flag.OUTSIDE, Flag.INDUCTION, Flag.BROADCAST);
		this.duration = notNull(duration);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Smokes some weed.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor is not holding a pipe or does not have any weed
	 */
	@RequiredObject(Light.TINDERBOX)
	@RequiredObject("pipe")
	public Response smoke(Entity actor) throws ActionException {
		final WorldObject weed = actor.contents().find(WEED).orElseThrow(() -> ActionException.of("smoke.requires.weed"));
		return smoke(actor, weed);
	}

	/**
	 * Smokes the given pipe-weed.
	 * @param actor		Actor
	 * @param weed		Pipe-weed to smoke
	 * @return Response
	 * @throws ActionException if the actor is not holding a pipe
	 */
	@RequiredObject(Light.TINDERBOX)
	@RequiredObject("pipe")
	public Response smoke(Entity actor, @Carried(auto=true) WorldObject weed) throws ActionException {
		final Induction induction = () -> {
			weed.destroy();
			return Response.of(new Description("action.smoke.result", weed.name()));
		};
		return Response.of(new Induction.Instance(induction, duration));
	}
}
