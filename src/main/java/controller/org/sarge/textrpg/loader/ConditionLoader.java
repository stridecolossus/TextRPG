package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.entity.Skill;

/**
 * Loader for a {@link Condition}.
 * @author Sarge
 */
public class ConditionLoader {
	/**
	 * Loads a condition.
	 * @param node Text-node
	 * @return Condition
	 */
	public Condition load(Element node) {
		switch(node.name()) {
		case "attribute":
			// TODO
		
		case "skill":
			final Skill skill;
			final int level = node.attributes().toInteger("level", null);
			// TODO
			return actor -> true;
			
		case "race":
			// TODO
			
		case "not":
			return Condition.invert(load(node.child()));
			
		case "compound":
			final List<Condition> conditions = node.children().map(this::load).collect(toList());
			if(conditions.isEmpty()) throw node.exception("Empty compound condition");
			return Condition.compound(conditions);
			
		default:
			throw node.exception("Unknown condition: " + node.name());
		}
	}
}
