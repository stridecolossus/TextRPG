package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Induction;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.world.Location;

/**
 * Action to recover arrows in the current location.
 * @author Sarge
 */
public class RecoverArrowsAction extends AbstractAction {
	private static final Predicate<WorldObject> ARROW = ContentsHelper.categoryMatcher("arrow");
	private static final Predicate<WorldObject> QUIVER = ContentsHelper.categoryMatcher("quiver");
	
	private final Skill archery;
	private final int mod;
	private final long duration;
	
	/**
	 * Constructor.
	 * @param archery 		Archery skill
	 * @param mod			Skill modifier
	 * @param duration		Base duration (ms)
	 */
	public RecoverArrowsAction(Skill archery, int mod, long duration) {
		Check.notNull(archery);
		Check.oneOrMore(mod);
		Check.oneOrMore(duration);
		this.archery = archery;
		this.mod = mod;
		this.duration = duration;
	}

	/**
	 * Recover arrows.
	 * @param actor
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse recover(Entity actor) throws ActionException {
		// Check terrain
		final Location loc = actor.location();
		switch(loc.getTerrain()) {
		case WATER:
			throw new ActionException("recover.invalid.terrain");
		}
		
		// Check archery skill
		final int level = getSkillLevel(actor, archery);
		final int score = level * mod; // TODO - perception modifier

		// Find an equipped quiver if any
		final Parent parent = find(actor, QUIVER, false).map(obj -> (Parent) obj).orElse(actor);
		
		// Start recovering
		final Induction induction = () -> {
			// Enumerate arrows found
			// TODO - create stack
			final List<WorldObject> results = loc.contents().stream()
				.filter(t -> t instanceof WorldObject)
				.filter(t -> t.visibility().intValue() > score)
				.map(t -> (WorldObject) t)
				.filter(ARROW)
				.collect(toList());
			
			// Notify
			if(results.isEmpty()) {
				return new Description("recover.found.none");
			}
			else {
				final String count = String.valueOf(results.size());
				results.stream().forEach(arrow -> arrow.setParentAncestor(parent));
				return new Description("recover.found.arrows", "count", count);
			}
		};
		return new ActionResponse("recover.start", induction, calculateDuration(this.duration, level));
	}
}
