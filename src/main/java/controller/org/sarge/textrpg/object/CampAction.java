package org.sarge.textrpg.object;

import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Induction;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.object.Light.Descriptor;
import org.sarge.textrpg.object.Light.Type;
import org.sarge.textrpg.world.Location;

/**
 * Action to build a camp-fire.
 * @author Sarge
 */
public class CampAction extends AbstractAction {
	private final Descriptor campfire;
	private final Skill skill;
	private final Predicate<WorldObject> wood;
	private final Predicate<WorldObject> tinderbox;
	private final long duration;

	/**
	 * Constructor.
	 * @param skill			Required skill
	 * @param campfire		Camp-fire descriptor
	 * @param lifetime		Lifetime of the camp-fire (minutes)
	 * @param wood			Fire-wood descriptor
	 * @param tinderbox		Tinderbox descriptor
	 * @param mod			Induction duration
	 */
	public CampAction(Skill skill, ObjectDescriptor campfire, int lifetime, ObjectDescriptor wood, ObjectDescriptor tinderbox, long duration) {
		Check.notNull(skill);
		Check.oneOrMore(duration);
		this.skill = skill;
		this.campfire = new Descriptor(campfire, lifetime, Type.CAMPFIRE);
		this.wood = ContentsHelper.objectMatcher(wood);
		this.tinderbox = ContentsHelper.objectMatcher(tinderbox);
		this.duration = duration;
	}
	
	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.MOUNTED};
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	/**
	 * Builds a camp in the current location.
	 * @param ctx
	 * @param actor
	 * @throws ActionException
	 */
	public ActionResponse camp(ActionContext ctx, Entity actor) throws ActionException {
		// Check camp can be built in this location
		final Location loc = actor.getLocation();
		switch(loc.getTerrain()) {
		case URBAN:
		case INDOORS:
		case WATER:
			throw new ActionException("camp.invalid.location");
		}
		
		// Check required skill
		final int level = getSkillLevel(actor, skill);
		
		// Check required components
		final WorldObject wood = find(actor, this.wood, false, "wood");
		final WorldObject tinderbox = find(actor, this.tinderbox, true, "tinderbox");

		// Calculate duration
		final long duration = calculateDuration(this.duration, level);

		// Start building camp-fire
		final Induction induction = () -> {
			// Create camp-fire and add to current location
			final Light light = new Light(campfire);
			light.setParent(loc);
			
			// Consume wood
			wood.destroy();
			
			// Apply wear to tinderbox
			tinderbox.wear();
			
			// Generate notifications
			// TODO
			
			// Build response
			return new Description("camp.response");
		};
		return new ActionResponse("camp.start", induction, duration);
	}
}
