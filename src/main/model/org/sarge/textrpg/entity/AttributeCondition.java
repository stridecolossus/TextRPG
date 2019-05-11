package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * Condition that requires the actor to have a minimum attribute value.
 * @author Sarge
 */
public class AttributeCondition extends AbstractEqualsObject implements Condition {
	private final Attribute attr;
	private final int min;
	private final Description description;

	/**
	 * Constructor.
	 * @param attr		Attribute
	 * @param min		Minimum attribute value
	 */
	public AttributeCondition(Attribute attr, int min) {
		this.attr = notNull(attr);
		this.min = oneOrMore(min);
		this.description = new Description.Builder("condition.attribute").name(TextHelper.prefix(attr)).add("min", min).build();
	}

	@Override
	public boolean matches(Actor actor) {
		final Entity entity = (Entity) actor;
		return entity.model().attributes().get(attr).get() >= min;
	}

	@Override
	public Description reason() {
		return description;
	}
}
