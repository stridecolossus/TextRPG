package org.sarge.textrpg.object;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Equipment;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.util.ActionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Recovers spent {@link Ammo} in the current location.
 * @author Sarge
 * TODO - recover all (any owner)
 */
@RequiresActor
@Component
public class RecoverAction extends SkillAction {
	private final InventoryController mover = new InventoryController("action.recover");

	/**
	 * Constructor.
	 * @param skill Archery skill
	 */
	public RecoverAction(@Value("#{skills.get('archery')}") Skill skill) {
		super(skill, Flag.LIGHT, Flag.INDUCTION);
	}

	/**
	 * Recovers spent ammo for the currently equipped weapon.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if no ranged weapon is equipped or the weapon uses {@link Ammo.Type#STONE}
	 */
	public Response recover(Entity actor) throws ActionException {
		// Check equipped ranged weapon
		final Equipment equipment = new Equipment();
		final Ammo.Type type = equipment.weapon().map(Weapon::descriptor).flatMap(Weapon.Descriptor::ammo).orElseThrow(() -> ActionException.of("recover.requires.weapon"));
		if(type == Ammo.Type.STONE) throw ActionException.of("recover.invalid.weapon");

		// Find ammo owned by this actor
		final Stream<Ammo> results = StreamUtil.select(HiddenObject.class, actor.location().contents().stream())
			.filter(obj -> obj.owner() == actor)
			.map(HiddenObject::object)
			.map(Ammo.class::cast)
			.filter(ammo -> ammo.descriptor().type() == type);

		// Convert to stacks
		final Collection<WorldObject> stacks = StreamUtil.select(Ammo.class, results)
			.map(Ammo::descriptor)
			.filter(ammo -> ammo.type() == type)
			.collect(groupingBy(Function.identity(), counting()))
			.entrySet()
			.stream()
			.map(entry -> new ObjectStack(entry.getKey(), entry.getValue().intValue()))
			.collect(toList());

		// Create induction
		final Induction induction = () -> {
			// Remove from location
			results.forEach(Ammo::destroy);

			// Add to quiver or inventory
			//final var dest = actor.equipment().quiver(type).map(Quiver::contents).orElse(actor.inventory());
			//final var responses = controller.move(stacks.stream(), dest, actor.destination().location().objects());
			// TODO
			return null;

			// Build response
			//return Response.of(responses);
		};

		// Build response
		final Skill skill = super.skill(actor);
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}
}
