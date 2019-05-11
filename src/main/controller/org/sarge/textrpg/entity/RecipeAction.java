package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Vocation.Recipe;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.Terrain;

/**
 * List recipes known by the player.
 * @author Sarge
 */
public class RecipeAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public RecipeAction() {
		super(Flag.OUTSIDE);
	}

	@Override
	protected boolean isValid(Stance stance) {
		return true;
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
	}

	@Override
	protected boolean isInductionValid() {
		return true;
	}

	/**
	 * Lists <b>all</b> recipes known by the actor.
	 * @param actor Actor
	 * @return Recipes
	 */
	public Response list(PlayerCharacter actor) {
		return null; // TODO
	}

	/**
	 * Lists <b>all</b> recipes for the given vocation known by the actor.
	 * @param actor 		Actor
	 * @param vocation		Vocation
	 * @return Recipes
	 */
	public Response list(PlayerCharacter actor, Vocation vocation) {
		final var recipes = actor.player().recipes().recipes(vocation);
		return null; // TODO
	}

	/**
	 * Lists recipes for the given vocation tier known by the actor.
	 * @param actor 		Actor
	 * @param vocation		Vocation
	 * @param tier			Tier 1..n
	 * @return Recipes
	 */
	public Response list(PlayerCharacter actor, Vocation vocation, Integer tier) {
		final var recipes = describe(actor.player().recipes(), vocation, tier);
		if(recipes.isEmpty()) {
			return Response.of("recipes.empty");
		}
		else {
			return Response.of(recipes);
		}
	}

	private static List<Description> describe(RecipeModel model, Vocation vocation, int tier) {
		return model.recipes(vocation, tier).map(RecipeAction::describe).collect(toList());
	}

	private static Description describe(Recipe recipe) {
		return new Description.Builder("recipe.description").name(recipe.name()).build();
	}
}
