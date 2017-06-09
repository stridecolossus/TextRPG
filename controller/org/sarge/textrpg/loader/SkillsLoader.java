package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.entity.Skill.Tier;
import org.sarge.textrpg.util.TextNode;

/**
 * Loader for skill descriptors.
 * @author Sarge
 */
public class SkillsLoader {
	private final ConditionLoader conditionLoader;
	
	public SkillsLoader(ConditionLoader conditionLoader) {
		Check.notNull(conditionLoader);
		this.conditionLoader = conditionLoader;
	}

	/**
	 * Loads a skill definition.
	 * @param xml XML
	 * @return Skill
	 */
	public Skill load(TextNode node) {
		final String name = node.getString("name", null);
		final List<Tier> tiers = node.children().map(this::loadTier).collect(toList());
		return new Skill(name, tiers);
	}
	
	/**
	 * Loads a skill-tier descriptor.
	 */
	private Tier loadTier(TextNode node) {
		final Condition condition = node.optionalChild().map(conditionLoader::load).orElse(Condition.TRUE);
		final int cost = node.getInteger("cost", null);
		return new Tier(condition, cost);
	}
}
