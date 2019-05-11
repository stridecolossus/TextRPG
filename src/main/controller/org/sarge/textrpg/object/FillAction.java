package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Predicate;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to fill a receptacle or lantern.
 * @author Sarge
 */
@Component
public class FillAction extends AbstractAction {
	private static final Predicate<Receptacle> OIL = ReceptacleController.matcher(Liquid.OIL);

	private final ReceptacleController controller;

	/**
	 * Constructor.
	 * @param controller Receptacle controller
	 */
	public FillAction(ReceptacleController controller) {
		super(Flag.OUTSIDE, Flag.BROADCAST);
		this.controller = notNull(controller);
	}

	/**
	 * Fills a water receptacle from an available source.
	 * @param actor		Actor
	 * @param rec		Receptacle to fill
	 * @return Response
	 * @throws ActionException if the receptacle cannot be filled
	 */
	@RequiresActor
	public Response fill(Entity actor, Receptacle rec) throws ActionException {
		final Receptacle src = water(actor);
		// TODO - cannot be resting if source is remote
		return fill(rec, src);
	}

	/**
	 * Fills a receptacle from the given source.
	 * @param rec Receptacle
	 * @param src Source
	 * @return Response
	 * @throws ActionException if the receptacle cannot be filled
	 */
	public Response fill(Receptacle rec, Receptacle src) throws ActionException {
		rec.fill(src);
		return response(rec);
	}

	/**
	 * Fills a lantern from an available receptacle.
	 * @param actor Actor
	 * @param light Light
	 * @return Response
	 * @throws ActionException if the receptacle cannot be filled
	 */
	@RequiresActor
	public Response fill(Entity actor, Light light) throws ActionException {
		final Receptacle src = actor.contents().select(Receptacle.class).filter(OIL).findAny().orElseThrow(() -> ActionException.of("fill.requires.oil"));
		return fill(light, src);
	}

	/**
	 * Fills a lantern from the given receptacle.
	 * @param light		Light to fill
	 * @param rec		Receptacle
	 * @return Response
	 * @throws ActionException if the receptacle cannot be filled
	 */
	public Response fill(Light light, Receptacle rec) throws ActionException {
		light.fill(rec);
		return response(rec);
	}

	/**
	 * Fills a cooking utensil from an available water source.
	 * @param utensil Utensil to fill
	 * @return Response
	 * @throws ActionException if the utensil is already filled
	 */
	@RequiresActor
	public Response fill(Entity actor, Utensil utensil) throws ActionException {
		return fill(utensil, water(actor));
	}

	/**
	 * Fills a cooking utensil from the given receptacle.
	 * @param utensil Utensil to fill
	 * @return Response
	 * @throws ActionException if the utensil is already filled
	 */
	public Response fill(Utensil utensil, Receptacle rec) throws ActionException {
		if(rec.descriptor().liquid() != Liquid.WATER) throw ActionException.of("fill.utensil.invalid");
		if(utensil.isWater()) throw ActionException.of("fill.utensil.already");
		if(rec.level() == 0) throw ActionException.of("receptacle.source.empty");
		rec.consume(1);
		utensil.water(true);
		return Response.OK;
	}

	/**
	 * Finds a water receptacle.
	 */
	private Receptacle water(Entity actor) throws ActionException {
		return controller.findWater(actor).orElseThrow(() -> ActionException.of("fill.requires.water"));
	}

	/**
	 * Builds a fill response.
	 * @param src Source receptacle
	 * @return Response
	 */
	private static Response response(Receptacle src) {
		if(src.isEmpty()) {
			return Response.of("receptacle.source.empty");
		}
		else {
			return Response.OK;
		}
	}
}
