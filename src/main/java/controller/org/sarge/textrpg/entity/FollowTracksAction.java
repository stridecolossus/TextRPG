package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
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
	private final MovementController mover;
	private final int mod;

	/**
	 * Constructor.
	 * @param track		Tracking skill
	 * @param period	Tracking iteration period (ms)
	 * @param mod		Movement cost modifier when tracking
	 */
	public FollowTracksAction(Skill track, long period, MovementController mover, int mod) {
		Check.notNull(track);
		Check.oneOrMore(period);
		Check.oneOrMore(mod);
		this.track = track;
		this.period = period;
		this.mover = notNull(mover);
		this.mod = mod;
	}

	// TODO - NOT string
	/**
	 * 
	 * @param actor
	 * @param race
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse follow(Entity actor, String race) throws ActionException {
		final int level = actor.getSkillLevel(track).orElseThrow(() -> new ActionException("track.requires.skill"));
		final Induction induction = new FollowInduction(actor, new TracksFollower(race, level), mover, mod, "follow.tracks.finished");
		return new ActionResponse("follow.tracks.start", induction, period);
	}
}
