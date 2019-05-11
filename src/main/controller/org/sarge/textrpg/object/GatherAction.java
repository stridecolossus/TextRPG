package org.sarge.textrpg.object;

import java.util.function.Function;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Randomiser;
import org.sarge.textrpg.world.Terrain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gathers resources in the current location, e.g. wood or sling stones.
 * @author Sarge
 * TODO - gather mushrooms?
 */
@Component
public class GatherAction extends SkillAction {
	private final Function<Terrain, Percentile> diff = t -> Percentile.ONE; // TODO

	/**
	 * Constructor.
	 * @param skill Wilderness-lore skill
	 */
	public GatherAction(@Value("#{skills.get('wilderness')}") Skill skill) {
		super(skill, Flag.LIGHT, Flag.INDUCTION);
	}

	/**
	 * Gathers resource.
	 * @param actor Actor
	 * @return Response
	 * TODO - how to make this work for both STONE and WOOD?
	 */
	@RequiresActor
	public Response gather(Entity actor) {
		// Create gather induction
		final Skill skill = super.skill(actor);
		final Induction induction = () -> {
			// Determine amount gathered
			final int count;
			final Percentile target = diff.apply(actor.location().terrain());
			if(super.isSuccess(actor, skill, target)) {
				final int max = skill.scale();
				count = Randomiser.range(max);
			}
			else {
				count = 0;
			}

			// Build response
			if(count == 0) {
				return Response.of("gather.failed");
			}
			else {
				// Add gathered resources to inventory
				// TODO - stones to ammo container
				// TODO - descriptor from registry
				final ObjectDescriptor descriptor = ObjectDescriptor.of("firewood");
				final ObjectStack stack = new ObjectStack(descriptor, count);

				// Add to inventory
				final InventoryController inv = new InventoryController("gather.firewood");
				final var result = inv.take(actor, stack);

				// Build response
				final Description header = new Description.Builder("gather.results").add("count", count).build();
				final Response.Builder builder = new Response.Builder().add(header);
				builder.add(result);
				return builder.build();
			}
		};

		// Build response
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
