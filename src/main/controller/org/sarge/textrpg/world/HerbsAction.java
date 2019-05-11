package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.Node;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to find and gather herb nodes.
 * @author Sarge
 */
@RequiresActor
@Component
public class HerbsAction extends SkillAction {
	/**
	 * Herbs node resource name.
	 */
	public static final String HERBS = "herbs";

	private final Duration forget;

	/**
	 * Constructor.
	 * @param skill 		Herb-lore skill
	 * @param forget		Node forget period
	 */
	public HerbsAction(@Value("#{skills.get('herblore')}") Skill skill, @Value("${herbs.forget}") Duration forget) {
		super(skill, Flag.LIGHT, Flag.INDUCTION, Flag.BROADCAST);
		this.forget = notNull(forget);
	}

	/**
	 * Finds herbs in the current location.
	 * @param actor Actor
	 * @return Response
	 */
	public Response find(PlayerCharacter actor) {
		// Find herbs node in this location
		// TODO - herbs difficulty ~ skill
		final var node = actor.location().contents()
			.select(Node.class)
			.filter(n -> n.descriptor().resource().equals(HERBS))
			.filter(n -> !actor.perceives(n))
			.findAny();

		// Create find induction
		final Induction induction = () -> {
			if(node.isPresent()) {
				final Node n = node.get();
				actor.hidden().add(n, forget);
				return Response.of(new Description("herbs.discovered", n.name()));
			}
			else {
				return Response.of("herbs.not.found");
			}
		};

		// Build response
		final Skill skill = super.skill(actor);
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}

	/**
	 * Culls discovered herbs in the current location.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if there are no herbs in the current location
	 */
	@RequiredObject("sickle")
	public Response cull(Entity actor) throws ActionException {
		// Find discovered herbs node
		final Node node = actor.location().contents()
			.select(Node.class)
			.filter(n -> n.descriptor().resource().equals(HERBS))
			.filter(actor::perceives)
			.findAny()
			.orElseThrow(() -> ActionException.of("cull.not.found"));

		// Cull node
		return cullLocal(actor, node);
	}

	/**
	 * Culls the specified herbs node.
	 * @param actor		Actor
	 * @param node		Herbs node
	 * @return Response
	 * @throws ActionException if the given node is not a herbs node
	 */
	@RequiredObject("sickle")
	public Response cull(Entity actor, Node node) throws ActionException {
		if(!node.descriptor().resource().equals(HERBS)) throw ActionException.of("cull.not.herbs");
		return cullLocal(actor, node);
	}

	/**
	 * Culls the given herbs node.
	 * @param actor		Actor
	 * @param node		Herbs node
	 * @return Response
	 */
	private static Response cullLocal(Entity actor, Node node) {
		// Collect node
		assert !node.isCollected();
		node.collect();

		// Gather herbs
		final LootFactory factory = actor.location().area().resource(HERBS).orElseThrow();
		final InventoryController inv = new InventoryController("gather.herbs");
		final var results = inv.take(actor, factory.generate(actor));

		// Build response
		return Response.of(results);
	}
}
