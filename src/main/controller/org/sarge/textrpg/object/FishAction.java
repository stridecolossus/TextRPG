package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Property;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Fishing action.
 * @author Sarge
 */
@Component
public class FishAction extends SkillAction {
	/**
	 * Fish resource-factory name.
	 */
	public static final String FISH = "fish";

	private static final String FAILED = "fish.failed";

	/**
	 * Constructor.
	 * @param skill Fishing skill
	 */
	public FishAction(@Value("#{skills.get('fish')}") Skill skill) {
		super(skill, Flag.LIGHT, Flag.OUTSIDE, Flag.REVEALS, Flag.INDUCTION, Flag.BROADCAST);
	}

	/**
	 * Fishes in the current location.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if cannot fish in the current location
	 */
	@RequiresActor
	@RequiredObject("fishing.rod")
	public Response fish(Entity actor) throws ActionException {
		// Check can fish in the current location
		final Location dest = actor.location();
		// TODO if(actor.parent() instanceof Boat)
		// TODO - can always fish from a boat?
		if(!dest.isProperty(Property.FISH)) throw ActionException.of("fish.invalid.location");

		// Find fishing loot-factory in this area or its ancestors
		final LootFactory factory = dest.area().resource(FISH).orElse(LootFactory.EMPTY);

		// Create fish induction
		final Skill skill = super.skill(actor);
		final Induction induction = () -> {
			if(super.isSuccess(actor, skill, Percentile.HALF)) {
				final var fish = factory.generate(actor).collect(toList());
				if(fish.isEmpty()) {
					// Empty catch
					return Response.of(FAILED);
				}
				else {
					// Add catch to inventory
					final InventoryController inv = new InventoryController("fish.success");
					final var results = inv.take(actor, fish.stream());
					return Response.of(results);
				}
			}
			else {
				// Failed catch
				return Response.of(FAILED);
			}
		};

		// Build response
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
