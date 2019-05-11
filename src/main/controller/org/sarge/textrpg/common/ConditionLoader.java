package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.AttributeCondition;
import org.sarge.textrpg.entity.RaceCondition;
import org.sarge.textrpg.entity.SkillCondition;
import org.sarge.textrpg.util.Registry;
import org.springframework.stereotype.Service;

/**
 * Loader for conditions.
 * @author Sarge
 */
@Service
public class ConditionLoader {
	private final Registry<Skill> skills;

	/**
	 * Constructor.
	 * @param skills Skills registry
	 */
	public ConditionLoader(Registry<Skill> skills) {
		this.skills = notNull(skills);
	}

	/**
	 * Loads a condition.
	 * @param xml XML
	 * @return Condition
	 */
	public Condition load(Element xml) {
		switch(xml.name()) {
		case "race":
			final String cat = xml.attribute("cat").toText();
			return new RaceCondition(cat);

		case "skill":
			final Skill skill = skills.get(xml.attribute("skill").toText());
			return new SkillCondition(skill);

		case "attribute":
			final Attribute attr = xml.attribute("attribute").toValue(Attribute.CONVERTER);
			final int min = xml.attribute("min").toInteger();
			return new AttributeCondition(attr, min);

		// TODO
		case "size":
		case "relationship":

		default:
			throw xml.exception("Invalid condition: " + xml.name());
		}
	}
}
