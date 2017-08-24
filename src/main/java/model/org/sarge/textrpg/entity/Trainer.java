package org.sarge.textrpg.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.object.ToString;
import org.sarge.textrpg.common.ActionException;

/**
 * Skills trainer.
 * @author Sarge
 */
public class Trainer {
	private final Set<Skill> skills;

	/**
	 * Constructor.
	 * @param skills Skills that this trainer can teach
	 */
	public Trainer(Set<Skill> skills) {
		this.skills = new HashSet<>(skills);
	}

	/**
	 * Lists the skills that this trainer can teach.
	 * @return Skills
	 */
	public Stream<Skill> list() {
		return skills.stream();
	}
	
	/**
	 * Trains the given skill.
	 * @param skill Skill to train
	 * @param actor Actor
	 * @throws ActionException if this entity cannot learn the given skill, has insufficient experience, or this trainer does not teach the skill
	 * @see SkillSet#increment(Skill, int)
	 */
	public void train(Skill skill, Entity actor) throws ActionException {
		// Check this trainer can teach the given skill
		if(!skills.contains(skill)) throw new ActionException("train.cannot.teach");
		
		// Check actor can learn the next level
		final int level = actor.getSkills().getLevel(skill).orElse(0);
		if(level >= skill.getMaximum()) throw new ActionException("train.maximum.level");
		
		// Check requirements
		final boolean matched = skill.getTier(level).getCondition().evaluate(actor);
		if(!matched) throw new ActionException("train.cannot.learn");
		
// TODO
//		// Check available XP
//		final int cost = skill.getTier(level).getCost();
//		if(actor.getValues().get(EntityValue.AVAILABLE_EXPERIENCE) < cost) {
//			throw new ActionException("train.insufficent.xp");
//		}
		
		// Increment skill level
		actor.getSkills().increment(skill, 1);
		
//		// Consume XP
//		actor.modify(EntityValue.AVAILABLE_EXPERIENCE, -cost);
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
