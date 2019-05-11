package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.ValueModifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Listens for nearby sounds (higher detect chance than passive ).
 * @author Sarge
 * TODO - generalize to detecting lights/smoke (look around) and smell (monsters only?)
 */
@Component
public class ListenAction extends AbstractAction {
	private final int mod;

	/**
	 * Constructor.
	 * @param mod Perception modifier
	 * TODO - skill modifier(s)
	 */
	public ListenAction(@Value("${listen.perception.modifier}") int mod) {
		super(Flag.OUTSIDE, Flag.INDUCTION);
		this.mod = oneOrMore(mod);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Listens for nearby noises.
	 * @param actor Actor
	 * @return Response
	 */
	@RequiresActor
	public Response listen(Entity actor) {
		// TODO - this would also enhance light/smoke!!!
		// Increment perception
		final ValueModifier perception = actor.model().modifier(Attribute.PERCEPTION);
		perception.modify(mod);

		// Decrement when stopped
		final Runnable stop = () -> {
			actor.alert(Description.of("action.listen.stop"));
			perception.modify(-mod);
		};

		// Listen indefinitely
		return new Response.Builder()
			.add("action.listen.start")
			.induction(Induction.Instance.indefinite(stop))
			.build();
	}
}
