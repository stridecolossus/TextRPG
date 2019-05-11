package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.springframework.stereotype.Component;

/**
 * Displays available exits from the current location.
 * @author Sarge
 */
@Component
public class ExitsAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public ExitsAction() {
		super(Flag.OUTSIDE);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Terrain terrain) {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Displays available exits.
	 * @param actor Actor
	 * @return Response
	 */
	@RequiresActor
	public Response exits(Entity actor) {
		// Build exit descriptions
		final var exits = actor.location().exits().stream()
			.filter(exit -> exit.isPerceivedBy(actor))
			.map(Exit::describe)
			.collect(toList());

		// TODO - partial?

		// Build response
		if(exits.isEmpty()) {
			return Response.of("list.exits.none");
		}
		else {
			final Response.Builder response = new Response.Builder();
			response.add("list.exits.header");
			exits.forEach(response::add);
			return response.build();
		}
	}
}
