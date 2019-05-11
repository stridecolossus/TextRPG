package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.springframework.stereotype.Component;

/**
 * Emote action.
 * @author Sarge
 */
@Component
@RequiresActor
public class EmoteAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public EmoteAction() {
		super(Flag.OUTSIDE, Flag.BROADCAST);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 *
	 * TODO
	 * - emote(Entity actor, Emote emote, [Entity entity])
	 * - optional entity
	 * - list emotes action
	 * - Emote arg matcher
	 * - duration / immediate / indefinite
	 * - displayed in describe()
	 *
	 */
}
