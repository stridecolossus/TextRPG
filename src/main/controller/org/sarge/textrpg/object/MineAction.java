package org.sarge.textrpg.object;

import java.util.stream.Stream;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.world.Area;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to mine a metal {@link Node}.
 * @author Sarge
 * TODO - check vocation/recipe?
 * @see Area.Resource#METALS
 */
@Component
public class MineAction extends SkillAction {
	/**
	 * Lode resource factory name.
	 */
	public static final String LODE = "lode";

	private static final ActionException MINED = ActionException.of("mine.already.mined");

	/**
	 * Constructor.
	 * @param skill Mining skill
	 */
	public MineAction(@Value("#{skills.get('mine')}") Skill skill) {
		super(skill, Flag.LIGHT, Flag.INDUCTION);
	}

	/**
	 * Mines the given node.
	 * @param actor		Actor
	 * @param node		Node to mine
	 * @return Response
	 * @throws ActionException if the node has already been mined
	 */
	@RequiresActor
	@RequiredObject("mining.pick")
	public Response mine(PlayerCharacter actor, Node node) throws ActionException {
		// Check actor has required vocation
		// TODO
		//actor.player().recipes();

		// Check can be mined
		final String res = node.descriptor().resource();
		if(!LODE.equals(res)) throw ActionException.of("mine.cannot.mine");

		// Check not already mined
		if(node.isCollected()) throw MINED;

		// Create induction
		final Induction induction = () -> {
			// Collect node
			if(node.isCollected()) throw MINED;
			node.collect();

			// Generate resources
			// TODO - results ~ skill (inject to loot-factory)
			final LootFactory factory = actor.location().area().resource(res).orElseThrow();
			final Stream<WorldObject> mined = factory.generate(actor);

			// Add to inventory
			final InventoryController inv = new InventoryController("mine.collect");
			final var results = inv.take(actor, mined);

			// Build response
			return Response.of(results);
		};

		// Build response
		final Skill skill = super.skill(actor);
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
