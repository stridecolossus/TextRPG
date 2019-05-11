package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.entity.Transaction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.LocationCache;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.Location;
import org.springframework.stereotype.Component;

/**
 * Action to use a town-gate.
 * @author Sarge
 * @see Gate
 * TODO - climb gate?
 */
@Component
public class GateAction extends AbstractAction {
	private final ArgumentParser.Registry registry;
	private final ObjectController controller;
	private final LocationCache<Gate> cache = new LocationCache<>(this::find);

	/**
	 * Constructor.
	 * @param parser 			Money argument parser
	 * @param controller		Object controller for reset events
	 */
	public GateAction(MoneyArgumentParser parser, ObjectController controller) {
		super(Flag.OUTSIDE, Flag.BROADCAST);
		this.registry = ArgumentParser.Registry.of(Money.class, parser);
		this.controller = notNull(controller);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		return registry;
	}

	/**
	 * Call for a gate to be opened/closed.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the gate is already open (during the day), the actor is not sufficiently known to the controlling faction, or the gate does not a keeper
	 */
	@RequiresActor
	public Response call(Entity actor) throws ActionException {
		// Check gate present
		final Gate gate = find(actor);

		// Check gate has a keeper that can be called
		final Gate.Keeper keeper = gate.descriptor().keeper().orElseThrow(() -> ActionException.of("gate.call.invalid"));

		// Check actor is known to the controlling faction
		if(!actor.isAssociated(keeper.association())) throw ActionException.of("call.faction.ignored");

		// Call gate
		final boolean open = gate.isOpen();
		gate.call();

		// Register reset event
		final Event reset = () -> {
			// Reset gate
			gate.reset();

			// Broadcast reset notification to both sides of the gate
			final Location loc = actor.location();
			final Location other = controller.other(loc, gate);
			final Description alert = new Description("gate.auto.reset", gate.name());
			loc.broadcast(null, alert);
			other.broadcast(null, alert);

			return false;
		};
		final Event.Reference ref = controller.reset(gate, reset);
		gate.holder().set(ref);

		// Build response
		final Description response = Description.of(TextHelper.join("action.call", !open ? "open" : "close"));
		return Response.of(response);
	}

	/**
	 * Bribes the gate-keeper to open the gate.
	 * @param actor		Actor
	 * @param bribe		Bribe amount
	 * @return Response
	 * @throws ActionException if the gate is already open (during the day), the gate-keeper cannot be bribed, or the bribe is insufficient
	 */
	public Response bribe(PlayerCharacter actor, Money bribe) throws ActionException {
		// Check gate present
		final Gate gate = find(actor);

		// Check gate-keeper can be bribed
		final int required = gate.descriptor().keeper().flatMap(Gate.Keeper::bribe).orElseThrow(() -> ActionException.of("gate.cannot.bribe"));
		if(bribe.value() < required) throw ActionException.of("gate.insufficient.bribe");

		// Consume money
		final Transaction tx = actor.settings().transaction(PlayerSettings.Setting.CASH, bribe.value(), "bribe.insufficient.money");
		tx.check();

		// Open gate
		gate.call();
		tx.complete();
		return Response.of("gate.bribe.success");
	}

	/**
	 * Finds the gate in the current location.
	 */
	private Gate find(Entity actor) throws ActionException {
		return cache.find(actor.location()).orElseThrow(() -> ActionException.of("gate.not.present"));
	}

	/**
	 * Mapper for cached gates.
	 */
	private Optional<Gate> find(Location loc) {
		return loc.exits().stream()
			.map(Exit::link)
			.map(Link::controller)
			.flatMap(Optional::stream)
			.filter(c -> c instanceof Gate)
			.map(Gate.class::cast)
			.findAny();
	}
}
