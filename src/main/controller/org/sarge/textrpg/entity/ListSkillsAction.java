package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Terrain;
import org.springframework.stereotype.Component;

/**
 * Action to list the skills of the actor.
 * @author Sarge
 */
@Component
public class ListSkillsAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public ListSkillsAction() {
		super(Flag.OUTSIDE);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	public boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Lists skills.
	 * @param actor Actor
	 * @return Skills response
	 */
	@RequiresActor
	public Response skills(PlayerCharacter actor) {
		// Enumerate skill names
		// TODO - organise by skill-group
		final List<Description> skills = actor.skills().stream()
			.map(Skill::name)
			.map(name -> TextHelper.join("skill", name))
			.map(Description::of)
			.collect(toList());

		// Build response
		if(skills.isEmpty()) {
			return Response.of("list.skills.none");
		}
		else {
			final Response.Builder builder = new Response.Builder();
			builder.add("list.skills.header");
			skills.forEach(builder::add);
			return builder.build();
		}
	}
}
