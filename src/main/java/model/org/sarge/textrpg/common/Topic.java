package org.sarge.textrpg.common;

import org.sarge.textrpg.entity.Trainer;
import org.sarge.textrpg.object.Shop;

/**
 * Conversation topic.
 */
public interface Topic {
	/**
	 * @return Topic name
	 */
	String getName();

	/**
	 * @return Script
	 */
	Script getScript();

	/**
	 * @return Shop
	 */
	Shop getShop();

	/**
	 * @return Trainer
	 */
	Trainer getTrainer();
}
