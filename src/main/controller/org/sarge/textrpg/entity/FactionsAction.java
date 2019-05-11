package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.function.Predicate;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Terrain;
import org.springframework.stereotype.Component;

/**
 * Action to list faction relationships.
 * @author Sarge
 */
@RequiresActor
@Component
public class FactionsAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public FactionsAction() {
		super(Flag.LIGHT);
	}

	@Override
	protected boolean isInductionValid() {
		return true;
	}

	@Override
	protected boolean isValid(Stance stance) {
		return true;
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Lists faction relationships within the current area.
	 * @param player Player
	 * @return Response
	 */
	public Response factions(PlayerCharacter player) {
		return list(player, association -> association.faction().area() == player.location().area());
	}

	/**
	 * Lists <b>all</b> faction relationships.
	 * @param player Player
	 * @param filter ALL argument (unused)
	 * @return Response
	 */
	public Response factions(PlayerCharacter player, ObjectDescriptor.Filter filter) {		// TODO - ALL argument?
		return list(player, ignore -> true);
	}

	/**
	 * Lists player factions.
	 */
	private static Response list(PlayerCharacter player, Predicate<Faction.Association> filter) {
		final var relations = player.player().associations().filter(filter).map(FactionsAction::describe).collect(toList());
		final var builder = new Response.Builder();
		builder.add("list.factions.header");
		builder.add(relations);
		return builder.build();
	}

	/**
	 * Helper - Describes a faction relationship.
	 */
	private static Description describe(Faction.Association association) {
		return new Description.Builder("list.factions.entry")
			.add("faction", association.faction().name())
			.add("level", TextHelper.prefix(association.relationship()))
			.build();
	}
}
