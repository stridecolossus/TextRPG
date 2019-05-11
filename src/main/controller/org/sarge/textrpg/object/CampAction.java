package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Action to build a camp-fire.
 * @author Sarge
 */
@Component
public class CampAction extends SkillAction {
	private final LightController controller;
	private final Light.Descriptor descriptor;

	/**
	 * Constructor.
	 * @param skill				Wilderness-lore skill
	 * @param controller		Light controller
	 * @param descriptor		Camp-fire descriptor
	 */
	public CampAction(@Value("#{skills.get('wilderness')}") Skill skill, LightController controller, Light.Descriptor descriptor) {
		super(skill, Flag.REVEALS, Flag.INDUCTION, Flag.BROADCAST);
		this.controller = notNull(controller);
		this.descriptor = notNull(descriptor);
	}

	/**
	 * Factory for the camp-fire descriptor.
	 * @param lifetime		Camp-fire lifetime
	 * @param light			Intensity of light emission
	 * @param smoke			Intensity of smoke emission
	 * @return Camp-fire descriptor
	 */
	@Bean
	public static Light.Descriptor descriptor(@Value("${camp.lifetime}") Duration lifetime, @Value("${camp.light.level}") Percentile light, @Value("${camp.smoke.level}") Percentile smoke) {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("campfire").fixture().build();
		return new Light.Descriptor(descriptor, Light.Type.CAMPFIRE, lifetime, light, smoke);
	}

	@Override
	protected boolean isValid(Stance stance) {
		if(stance == Stance.RESTING) {
			return true;
		}
		else {
			return super.isValid(stance);
		}
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		switch(terrain) {
		case INDOORS:
		case DARK:
		case URBAN:
			return false;

		default:
			return super.isValid(terrain);
		}
	}

	/**
	 * Builds a camp in the current location.
	 * @param actor				Actor
	 * @param wood				Wood to build the camp
	 * @return Response
	 * @throws ActionException if the current location already contains a camp-fire
	 */
	@RequiresActor
	@RequiredObject(Light.TINDERBOX)
	public Response camp(Entity actor, @RequiredObject("firewood") WorldObject wood) throws ActionException {
		// Check for an existing camp
		final Location loc = actor.location();
		if(Light.find(loc, Light.Type.CAMPFIRE).isPresent()) throw ActionException.of("camp.already.present");

		// Calculate duration
		final Skill skill = super.skill(actor);
		final Duration duration = skill.duration();

		// Create induction
		final Induction induction = () -> {
			// Build camp
			final Light camp = descriptor.create();
			camp.parent(loc);

			// Consume fuel
			wood.destroy();

			// Light camp
			controller.light(actor, camp);

			// Camp completed
			return Response.of("camp.completed");
		};

		// Build response
		return new Response.Builder()
			.add("camp.started")
			.induction(new Induction.Instance(induction, duration))
			.build();
	}
}
