package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractActiveAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Induction;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.world.Area.Resource;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

/**
 * Action to gather a {@link Resource} from the current location.
 * @author Sarge
 */
public class GatherAction extends AbstractActiveAction {
	private final Resource res;
	private final Skill skill;
	private final long duration;
	private final ObjectDescriptor wood;
	private final Optional<ObjectDescriptor> tool;

	/**
	 * Constructor.
	 * @param res		    Resource to gather
	 * @param skill			Required skill
	 * @param duration     Base duration (ms)
	 * @param wood         Fire-wood descriptor
	 * @param tool         Optional required gathering tool
	 */
	public GatherAction(Resource res, Skill skill, long duration, ObjectDescriptor wood, ObjectDescriptor tool) {
		super(res.name());
		Check.notNull(res);
		if(res == Resource.FISH) throw new IllegalArgumentException("Fish resource cannot be gathered");
		Check.notNull(skill);
		Check.oneOrMore(duration);
		this.res = res;
		this.skill = skill;
		this.duration = duration;
		this.wood = Check.notNull(wood);
		this.tool = Optional.ofNullable(tool);
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}

	/**
	 * Gather resources.
	 * @param actor
	 * @throws ActionException
	 */
	public ActionResponse gather(Entity actor) throws ActionException {
		// Check can gather in the current location
		final Location loc = actor.location();
		if(!isValidLocation(loc)) throw new ActionException("gather.invalid.location");

		// Check required skill
		final int level = getSkillLevel(actor, skill);

		// Check for required tool if any
		if(tool.isPresent()) {
			final Predicate<WorldObject> matcher = ContentsHelper.objectMatcher(tool.get());
			final WorldObject obj = find(actor, matcher, false, tool.get().getName());
			obj.use();
		}

		// Gather resources and add to inventory
		final Induction induction = () -> {
			final Stream<WorldObject> results = getResourceFactory(loc, res, level).map(f -> f.generate(actor)).orElse(null);
			if(results == null) {
				return new Description("gather.results.empty");
			}
			else {
				final List<WorldObject> list = results.collect(toList());
				list.stream().forEach(t -> t.setParentAncestor(actor));
				return new Description("gather.results." + res, "count", list.size());
			}
		};
		return new ActionResponse("gather.start", induction, calculateDuration(this.duration, level));
	}

	/**
	 * Looks up specified resource factory from the given location.
	 * @param loc Location
	 * @param res Resource type
	 * @return Resource factory if available
	 */
	private Optional<LootFactory> getResourceFactory(Location loc, Resource res, int level) {
	    if(res == Resource.WOOD) {
	        final int amount = getWoodAmount(loc.getTerrain());
	        if(amount == 0) {
	            // No wood available to be gathered
	            return Optional.empty();
	        }
	        else {
	            // Create loot-factory for gathered wood
	            return Optional.ofNullable(LootFactory.object(wood, amount));
	        }
	    }
	    else {
	        // Delegate to area
	        return loc.getArea().getResource(res);
	    }
	}

	// TODO - move to terrain-data-table
	private static int getWoodAmount(Terrain terrain) {
	    switch(terrain) {
	        case FOREST:       return 3;
	        case WOODLAND:     return 2;
	        case JUNGLE:       return 1;
	        default:           return 0;
	    }
	}

	/**
	 * @param loc Location
	 * @return Whether this given location is valid for gathering
	 */
	private static boolean isValidLocation(Location loc) {
		switch(loc.getTerrain()) {
		case DESERT:
		case ICE:
		case SNOW:
		case URBAN:
		case INDOORS:
		case UNDERGROUND:
			return false;

		default:
			return true;
		}
	}
}
