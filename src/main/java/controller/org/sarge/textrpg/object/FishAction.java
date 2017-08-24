package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Induction;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.Randomiser;
import org.sarge.textrpg.world.Area.Resource;
import org.sarge.textrpg.world.Location;

/**
 * Fishing action.
 * @author Sarge
 * TODO - bait: auto-use? or another action to bait the rod?
 */
public class FishAction extends AbstractAction {
	/**
	 * Fishing rod category.
	 */
	public static final String FISHING_ROD_CATEGORY = "fishing.rod";

	private static final Predicate<WorldObject> ROD_MATCHER = ContentsHelper.categoryMatcher(FISHING_ROD_CATEGORY);

	private final Skill skill;
	private final long period;

	/**
	 * Constructor.
	 * @param skill			Fishing skill
	 * @param period		Iteration period (ms)
	 */
	public FishAction(Skill skill, long period) {
		Check.notNull(skill);
		Check.oneOrMore(period);
		this.skill = skill;
		this.period = period;
	}

	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}

	@Override
	public ActionResponse execute(Entity actor) throws ActionException {
		// Check require skill
		final int level = actor.getSkillLevel(skill).orElseThrow(() -> new ActionException("fish.requires.skill"));
		
		// Check location
		final Location loc = actor.getLocation();
		if(!loc.isWaterAvailable()) throw new ActionException("fish.requires.water");
		
		// Check for rod
		final Optional<WorldObject> rod = actor.getEquipment().get(DeploymentSlot.MAIN_HAND);
		if(!rod.map(ROD_MATCHER::test).orElse(false)) throw new ActionException("fish.requires.rod");
		// TODO - auto-equip?

		// Lookup fishing loot-factory
		final Optional<LootFactory> factory = loc.getArea().getResource(Resource.FISH);
		
		// Start fishing
		final Induction induction = () -> {
			// Ignore if no fish
			if(!factory.isPresent()) return null;
			
			// Check for catch
			final boolean caught = Randomiser.range(100) > level;
			
			// Generate loot
			if(caught) {
				final List<WorldObject> fish = factory.get().generate(actor).collect(toList());
				final List<Description> desc = fish.stream().map(WorldObject::describe).collect(toList());
				actor.getNotificationHandler().handle(Description.create("fish.catch", desc).toNotification());
				fish.forEach(f -> f.setParentAncestor(actor));
			}
			
			return null;
		};
		return new ActionResponse("fish.start", induction, period, true);
	}
}
