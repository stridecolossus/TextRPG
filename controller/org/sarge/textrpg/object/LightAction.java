package org.sarge.textrpg.object;

import java.util.function.Predicate;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Light.Operation;

/**
 * Action to manipulate a {@link Light}.
 * @author Sarge
 */
public class LightAction extends AbstractAction {
	private static final Predicate<WorldObject> TINDERBOX = ContentsHelper.categoryMatcher("tinderbox");
	
	private final Operation op;

	/**
	 * Constructor.
	 * @param op Light operation
	 */
	public LightAction(Operation op) {
		super(op.name());
		Check.notNull(op);
		this.op = op;
	}

	@Override
	public boolean isLightRequiredAction() {
		return false;
	}
	
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Light action.
	 * @param ctx
	 * @param actor
	 * @param light
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, Light light) throws ActionException {
		// Perform action
		verifyCarried(actor, light);
		switch(op) {
		case LIGHT:
			final WorldObject tinderbox = find(actor, TINDERBOX, false, "tinderbox");
			light.execute(op, ctx.getTime());
			tinderbox.wear();
			break;
			
		default:
			light.execute(op, ctx.getTime());
			break;
		}
		
		// Build response
		return new ActionResponse(buildResponse(light, op));
	}
	
	/**
	 * Build light action response.
	 * @param light		Light
	 * @param op		Action
	 * @return Response
	 */
	public static Description buildResponse(Light light, Operation op) {
		return new Description.Builder("light.response")
			.wrap("name", light)
			.wrap("op", "light." + op)
			.build();
	}
}
