package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;

/**
 * Action to train {@link Skill}.
 * @author Sarge
 */
public class TrainAction extends AbstractAction {
	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
	}
	
	public ActionResponse train(Entity actor, String name) throws ActionException {
		// Check for trainer in this location
		
//		actor.getLocation().getContents().stream()
		
		Trainer trainer = new Trainer(null);
	//	final Trainer trainer = null; // actor.getLocation().getTrainer().orElseThrow(() -> new ActionException("train.no.trainer"));
		
		// Check trainer can train this skill
		final Skill skill = trainer.list().filter(sk -> sk.getName().equals(name)).findFirst().orElseThrow(() -> new ActionException("train.invalid.skill"));
		
		// Train skill and consume XP
		trainer.train(skill, actor);
		
		return null;
	}
}
