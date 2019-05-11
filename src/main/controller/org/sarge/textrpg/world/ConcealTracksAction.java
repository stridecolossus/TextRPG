package org.sarge.textrpg.world;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to cover tracks.
 * @author Sarge
 * @see Tracks#conceal(Percentile)
 */
@Component
public class ConcealTracksAction extends SkillAction {
	/**
	 * Constructor.
	 * @param skill Tracking skill
	 */
	public ConcealTracksAction(@Value("#{skills.get('track')}") Skill skill) {
		super(skill, Flag.BROADCAST);
	}

	/**
	 * Conceals <b>all</b> tracks in the previous location of the given actor.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if there are no tracks to hide
	 */
	@RequiresActor
	// TODO - means can access previous location that may be traversable from current location!
	public Response conceal(Entity actor) throws ActionException {
		// Check tracks to cover
		final Trail trail = actor.model().trail();
		final Tracks tracks = trail.previous();
		if(tracks == null) throw ActionException.of("tracks.cover.none");

		// Create cover induction
		final Skill skill = super.skill(actor);
		final Induction induction = () -> {
			if(super.isSuccess(actor, skill, tracks.visibility())) {
				final Percentile mod = skill.score();
				actor.location().tracks().forEach(t -> t.conceal(mod));
				return Response.of("tracks.cover.finished");
			}
			else {
				return Response.of("tracks.cover.failed");
			}
		};

		// Build response
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
