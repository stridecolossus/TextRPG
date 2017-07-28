package org.sarge.textrpg.loader;

import java.util.Map;

import org.sarge.lib.util.StrictMap;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.entity.SkillSet;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.util.TextNode;

/**
 * Loader for a {@link SkillSet}.
 * @author Sarge
 */
public class SkillSetLoader {
	private final Registry<Skill> skills;
	
	/**
	 * Constructor.
	 * @param skills Skill definitions
	 */
	public SkillSetLoader(Registry<Skill> skills) {
		this.skills = skills;
	}
	
	/**
	 * Loads a skill-set.
	 * @param xml XML
	 * @return Skill-set
	 */
	public SkillSet load(TextNode node) {
		final Map<Skill, Integer> map = new StrictMap<>();
		node.children("skill").forEach(e -> load(e, map));
		return new SkillSet(map);
	}
	
	/**
	 * Adds a skill.
	 */
	private void load(TextNode node, Map<Skill, Integer> map) {
		final String name = node.getString("name", null);
		final Skill skill = skills.find(name);
		final int level = node.getInteger("level", null);
		map.put(skill, level);
	}
}
