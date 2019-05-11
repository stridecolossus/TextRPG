package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.object.ObjectHelper;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to bandage a {@link Wound}.
 * @author Sarge
 */
@RequiresActor
@Component
public class BandageAction extends SkillAction {
	/**
	 * Constructor.
	 * @param skill Bandage skill
	 */
	public BandageAction(@Value("#{skills.get('bandage')}") Skill skill) {
		super(skill, Flag.LIGHT, Flag.INDUCTION);
	}

	@Override
	public boolean isValid(Stance stance) {
		if(stance == Stance.RESTING) {
			return true;
		}
		else {
			return super.isValid(stance);
		}
	}

	/**
	 * Bandages a wound.
	 * @param actor			Actor
	 * @param bandages		Bandages
	 * @return Response
	 * @throws ActionException if the entity cannot be bandaged
	 */
	public Response bandage(Entity actor, @RequiredObject("bandage") WorldObject bandages) throws ActionException {
		return bandage(actor, bandages, actor);
	}

	/**
	 * Bandages a wounded entity.
	 * @param actor			Actor
	 * @param bandages		Bandages
	 * @param entity		Entity to bandage
	 * @return Response
	 * @throws ActionException if the entity cannot be bandaged
	 */
	public Response bandage(Entity actor, @RequiredObject("bandage") WorldObject bandages, Entity entity) throws ActionException {
		// Check valid entity
		if(actor.isValidTarget(entity)) throw ActionException.of("bandage.invalid.target");

// TODO
//		// Check for wounds to bandage
//		if(!entity.applied().anyMatch(e -> e.group() == Effect.Group.WOUND)) {
//			throw ActionException.of("bandage.not.wounded");
//		}

		// Create bandage induction
		final Skill skill = super.skill(actor);
		final Induction induction = () -> {
			// Find wound to bandage
			final Percentile score = skill.score();
// TODO
//			final List<AppliedEffect> results = entity.dispel(Effect.Group.WOUND, score.intValue(), 1);
//
//			// Check can bandage wound
//			if(results.isEmpty()) {
//				return Response.of(new Description("bandage.cannot.bandage"));
//			}

			// TODO - replace with bandaged wound effect
			// TODO - partial success? e.g. poorly bound

			// Consume bandage
			ObjectHelper.destroy(bandages);

			// Build response
			// TODO - poorly, etc ~ score
			final Description response = new Description("bandage.success", entity.name());
			return Response.of(response);
		};

		// Build response
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
