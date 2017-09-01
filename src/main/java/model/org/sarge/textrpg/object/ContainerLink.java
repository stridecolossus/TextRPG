package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StringUtil;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Description.Builder;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.ExtendedLink;
import org.sarge.textrpg.world.Route;

/**
 * Link that requires an object to traverse the link, e.g. a ladder.
 * @author Sarge
 */
public class ContainerLink extends ExtendedLink {
	private final ObjectDescriptor descriptor;
	private final Optional<Thing> controller;

	private WorldObject obj;

	/**
	 * Constructor.
	 * @param route			Route-type
	 * @param script		Script
	 * @param size			Link size
	 * @param name			Controller name, e.g. a chasm
	 * @param descriptor	Descriptor for object required for this link
	 */
	public ContainerLink(Route route, Script script, Size size, String name, ObjectDescriptor descriptor) {
		super(route, script, size);
		Check.notEmpty(name);
		Check.notNull(descriptor);
		this.descriptor = descriptor;
		this.controller = Optional.of(createController(name, Percentile.ONE, true, 0));
	}

	@Override
	public Optional<Thing> getController() {
		if(obj == null) {
			return controller;
		}
		else {
			return Optional.of(obj);
		}
	}

	@Override
	public String reason(Actor actor) {
        update();
	    if(obj == null) {
	        return "move.link.object";
	    }
	    else {
	        return super.reason(actor);
	    }
	}

	@Override
	public String describe(String dir) {
		if(obj == null) {
			return StringUtil.wrap(super.describe(dir), "{", "}");
		}
		else {
			return super.describe(dir);
		}
	}

	@Override
	public Builder describe() {
		return new Description.Builder("exit.object").wrap("name", controller.get().toString());
	}

	/**
	 * Puts the given object into this link.
	 * @param obj Object
	 * @throws ActionException if the object is not the right type or this link is already occupied
	 */
	protected void put(WorldObject obj) throws ActionException {
		update();
		if(obj.getDescriptor() != descriptor) throw new ActionException("object.link.invalid");
		if(this.obj != null) throw new ActionException("object.link.occupied");
		this.obj = obj;
		obj.hide();
	}

	/**
	 * Clears the object if it has been removed.
	 */
	private void update() {
		if((obj != null) && (obj.getParent() != Thing.LIMBO)) {
			obj = null;
		}
	}
}
