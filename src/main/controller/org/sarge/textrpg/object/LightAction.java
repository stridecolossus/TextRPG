package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.EnumAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * Action to manipulate a light.
 * @author Sarge
 */
@EnumAction(LightAction.Operation.class)
public class LightAction extends AbstractAction {
	/**
	 * Light operations.
	 */
	public enum Operation {
		LIGHT {
			@Override
			protected void execute(Entity actor, Light light, LightController controller) throws ActionException {
				controller.light(actor, light);
			}
		},

		SNUFF {
			@Override
			protected void execute(Entity actor, Light light, LightController controller) throws ActionException {
				controller.snuff(light);
				light.expiry().cancel();
				light.warning().cancel();
			}
		},

		COVER {
			@Override
			protected void execute(Entity actor, Light light, LightController controller) throws ActionException {
				light.cover();
				LightController.notify(light);
			}
		},

		UNCOVER {
			@Override
			protected void execute(Entity actor, Light light, LightController controller) throws ActionException {
				light.uncover();
				LightController.notify(light);
			}
		};

		/**
		 * Performs this operation on the given light.
		 * @param actor 			Actor
		 * @param light				Light
		 * @param controller		Controller
		 * @throws ActionException if this operation cannot be performed
		 */
		protected abstract void execute(Entity actor, Light light, LightController controller) throws ActionException;
	}

	private final LightController controller;

	/**
	 * Constructor.
	 * @param controller Light controller
	 */
	public LightAction(LightController controller) {
		super(Flag.OUTSIDE, Flag.BROADCAST);
		this.controller = notNull(controller);
	}

	/**
	 * Applies a light operation.
	 * @param actor			Actor
	 * @param op			Light operation
	 * @param light			Light
	 * @return Response
	 * @throws ActionException if the operation cannot be performed
	 */
	@RequiresActor
	public Response apply(Entity actor, Operation op, Light light) throws ActionException {
		final String key = TextHelper.join("action.light", op.name());
		final Description.Builder response = new Description.Builder(key).name(light.name());
		op.execute(actor, light, controller);
		return Response.of(response.build());
	}
}
