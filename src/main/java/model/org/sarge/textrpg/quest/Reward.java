package org.sarge.textrpg.quest;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.WorldObject;

/**
 * Quest rewards.
 * @author Sarge
 * TODO
 * - xp/points
 * - achievement start/inc
 * - skill/inc
 * - faction rep
 */
public interface Reward {
	/**
	 * Rewards the given player.
	 * @param player Player
	 */
	void reward(Player player);

	/**
	 * Empty rewards.
	 */
	Reward NONE = player -> {
		// Does nothing
	};

	/**
	 * Experience reward.
	 * @param xp Experience
	 * @return XP reward
	 */
	static Reward experience(int xp) {
		return player -> player.modify(EntityValue.EXPERIENCE, xp);
	}

	/**
	 * Awards loot.
	 * @param factory Loot-factory
	 * @return Loot reward
	 */
	static Reward loot(LootFactory factory) {
		return player -> {
			final Stream<WorldObject> loot = factory.generate(player);
			loot.forEach(obj -> obj.setParentAncestor(player));
		};
	}
	
	/**
	 * Compound reward.
	 * @param rewards Rewards
	 * @return Compound reward
	 */
	static Reward compound(List<Reward> rewards) {
		Check.notEmpty(rewards);
		return player -> rewards.stream().forEach(reward -> reward.reward(player));
	}
}
