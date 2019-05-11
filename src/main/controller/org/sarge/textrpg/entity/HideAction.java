package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Function;

import org.sarge.textrpg.common.EffortAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Terrain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to hide in the current location.
 * @author Sarge
 */
@Component
@EffortAction
public class HideAction extends SkillAction {
	private Function<Terrain, Percentile> terrain = t -> Percentile.ONE;

	/**
	 * Constructor.
	 * @param skill		Hide skill
	 * @param mod		Terrain modifier
	 */
	public HideAction(@Value("#{skills.get('hide')}") Skill skill) {
		super(skill, Flag.LIGHT, Flag.INDUCTION);
	}

	/**
	 * Sets the terrain modifier.
	 * @param terrain Terrain modifier
	 */
	@Autowired
	public void setTerrainModifier(@Value("#{terrain.function('hide', T(org.sarge.lib.util.Converter).INTEGER)}") Function<Terrain, Percentile> terrain) {
		this.terrain = notNull(terrain);
	}

	/**
	 * Hides in the current location.
	 * @param actor 		Actor
	 * @param effort		Effort
	 * @return Response
	 * @throws ActionException if already hiding
	 * TODO - skill? other modifiers?
	 */
	@RequiresActor
	public Response hide(Entity actor, Effort effort) throws ActionException {
		// Check can hide
		if(actor.model().stance() == Stance.HIDING) throw ActionException.of("hide.already.hiding");

		// Create hide induction
		final Skill skill = super.skill(actor);
		final Induction induction = () -> {
			// Remove sneaking modifier
			final var model = actor.model();
			final Visibility vis = model.values().visibility();
			if(model.stance() == Stance.SNEAKING) {
				vis.remove();
			}

			// Calculate visibility including terrain modifier
			final Percentile scale = this.terrain.apply(actor.location().terrain());
			final Percentile score = skill.score().scale(scale);

			// Hide
			model.stance(Stance.HIDING);
			vis.stance(score);
			return Response.EMPTY;
		};

		// Start hiding
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
