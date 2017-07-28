package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description.Builder;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.world.ExtendedLink;
import org.sarge.textrpg.world.Route;

/**
 * Link controlled by a non-portal object, e.g. a blockage.
 * @author Sarge
 * TODO - extends portal link?
 */
public class ObjectLink extends ExtendedLink {
	private final WorldObject obj;
	private final String reason;

	/**
	 * Controller.
	 * @param route			Route-type
	 * @param script		Script
	 * @param size			Size constraint
	 * @param obj			Controller
	 * @param reason		Reason code if the object is <i>closed</i>
	 * @throws IllegalArgumentException if the object is not {@link Openable}
	 */
	public ObjectLink(Route route, Script script, Size size, WorldObject obj, String reason) {
		super(route, script, size);
		Check.notEmpty(reason);
		if(!obj.getOpenableModel().isPresent()) throw new IllegalArgumentException("Object must be openable");
		this.obj = obj;
		this.reason = reason;
	}

	@Override
	public Optional<Thing> getController() {
		return Optional.of(obj);
	}

	@Override
	public boolean isTraversable(Actor actor) {
		return isOpen() && super.isTraversable(actor);
	}

	@Override
	public String getReason() {
		if(!isOpen()) {
			return reason;
		}
		else {
			return super.getReason();
		}
	}

	@Override
	public Builder describe() {
		final Builder builder = super.describe();
		if(!isOpen()) {
			builder.wrap("object", obj.getName());
		}
		return builder;
	}

	/**
	 * @return Whether this link is open
	 */
	public boolean isOpen() {
		return obj.getOpenableModel().get().isOpen();
	}
}