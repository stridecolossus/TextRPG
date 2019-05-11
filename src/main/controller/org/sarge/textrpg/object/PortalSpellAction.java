package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Consumer;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Portal.PortalState;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to cast a portal spell.
 * @author Sarge
 * TODO - some sort of 'knock' spell that unlocks & opens a locked door? or restrict to burglars?
 */
@RequiresActor
@Component
public class PortalSpellAction extends AbstractAction {
	private final Skill breakPortal, blockPortal;
	private final ObjectController controller;

	/**
	 * Constructor.
	 * @param breakPortal		Spell to break a portal
	 * @param blockPortal		Spell to block a portal
	 * @param controller		Object controller for portal notifications
	 */
	public PortalSpellAction(@Value("#{skills.get('break')}") Skill breakPortal, @Value("#{skills.get('block')}") Skill blockPortal, ObjectController controller) {
		super(Flag.OUTSIDE, Flag.INDUCTION, Flag.BROADCAST);
		this.breakPortal = notNull(breakPortal);
		this.blockPortal = notNull(blockPortal);
		this.controller = notNull(controller);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	/**
	 * Casts a block-portal spell.
	 * @param actor			Actor
	 * @param portal		Portal
	 * @return Response
	 * @throws ActionException if the portal cannot be blocked
	 */
	public Response block(Entity actor, Portal portal) throws ActionException {
		return cast(actor, blockPortal, portal, Portal::block);
	}

	/**
	 * Casts a break-portal spell.
	 * @param actor			Actor
	 * @param portal		Portal
	 * @return Response
	 * @throws ActionException if the portal cannot be broken
	 */
	public Response breakPortal(Entity actor, Portal portal) throws ActionException {
		return cast(actor, breakPortal, portal, Portal::destroy);
	}

	/**
	 * Casts a portal spell.
	 * @param actor			Actor
	 * @param spell			Spell controller
	 * @param portal		Portal
	 * @param action		Portal action
	 * @return Response
	 * @throws ActionException if the portal cannot be broken
	 */
	private Response cast(Entity actor, Skill spell, Portal portal, Consumer<Portal> action) throws ActionException {
		// Check spell can be cast
		check(portal, spell);

		// Create spell induction
		final Induction induction = () -> {
			// Check can still be applied
			check(portal, spell);

			// Determine outcome
			final boolean success = super.isSuccess(actor, spell, Percentile.HALF); // TODO - some sort of resistance on Portal?

			// Apply action
			if(success) {
				action.accept(portal);
			}

			// Notify other side of portal
			if(spell == breakPortal) {
				final Location other = controller.other(actor.location(), portal);
				other.broadcast(null, new Description("notification.portal.broken", portal.name()));
			}

			// Build response
			final String str = TextHelper.join(spell.name(), success ? "success" : "failed");
			return AbstractAction.response(str, portal.name());
		};

		// Build response
		return Response.of(new Induction.Instance(induction, spell.duration()));
	}

	/**
	 * @throws ActionException if a spell cannot be cast on the given portal
	 */
	private static void check(Portal portal, Skill spell) throws ActionException {
		if(portal.state() != PortalState.DEFAULT) throw ActionException.of(TextHelper.join(spell.name(), "invalid"));
	}
}
