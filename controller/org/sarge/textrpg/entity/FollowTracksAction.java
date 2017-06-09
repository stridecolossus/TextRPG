package org.sarge.textrpg.entity;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.world.TracksFollower;

/**
 * Action to follow something.
 * @author Sarge
 */
public class FollowTracksAction extends AbstractAction {
	private final Skill track;
	private final long period;
	private final int mod;

	/**
	 * Constructor.
	 * @param track		Tracking skill
	 * @param period	Tracking iteration period (ms)
	 * @param mod		Movement cost modifier when tracking
	 */
	public FollowTracksAction(Skill track, long period, int mod) {
		Check.notNull(track);
		Check.oneOrMore(period);
		Check.oneOrMore(mod);
		this.track = track;
		this.period = period;
		this.mod = mod;
	}

	// TODO - NOT string
	/**
	 * 
	 * @param ctx
	 * @param actor
	 * @param race
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse follow(ActionContext ctx, Entity actor, String race) throws ActionException {
		final int level = actor.getSkillLevel(track).orElseThrow(() -> new ActionException("track.requires.skill"));
		final Induction induction = new FollowInduction(ctx, actor, new TracksFollower(race, level), mod, "follow.tracks.finished");
		return new ActionResponse("follow.tracks.start", induction, period);
	}
}
