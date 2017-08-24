package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
import org.sarge.textrpg.world.Area.Resource;
import org.sarge.textrpg.world.Location;

/**
 * Action to gather a {@link Resource} from the current location.
 * @author Sarge
 */
public class GatherAction extends AbstractAction {
	private final Resource res;
	private final Skill skill;
	private final long duration;
	private final Optional<ObjectDescriptor> tool;
	
	/**
	 * Constructor.
	 * @param res			Resource to gather
	 * @param skill			Required skill
	 * @param matcher		Optional required gathering tool
	 */
	public GatherAction(Resource res, Skill skill, long duration, ObjectDescriptor tool) {
		super(res.name());
		Check.notNull(res);
		if(res == Resource.FISH) throw new IllegalArgumentException("Fish resource cannot be gathered");
		Check.notNull(skill);
		Check.oneOrMore(duration);
		this.res = res;
		this.skill = skill;
		this.duration = duration;
		this.tool = Optional.ofNullable(tool);
	}
	
	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
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
		final Location loc = actor.getLocation();
		if(!isValidLocation(loc)) throw new ActionException("gather.invalid.location");
		
		// Check required skill
		final int level = getSkillLevel(actor, skill);

		// Check for required tool if any
		if(tool.isPresent()) {
			final Predicate<WorldObject> matcher = ContentsHelper.objectMatcher(tool.get());
			final WorldObject obj = find(actor, matcher, false, tool.get().getName());
			obj.wear();
		}
		
		// Gather resources and add to inventory
		final Induction induction = () -> {
			final Stream<WorldObject> results = loc.getArea().getResource(res).map(f -> f.generate(actor)).orElse(null);
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
