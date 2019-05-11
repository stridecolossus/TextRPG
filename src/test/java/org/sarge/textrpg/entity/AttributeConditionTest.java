package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.EnumerationIntegerMap;

public class AttributeConditionTest {
	private Condition condition;

	@BeforeEach
	public void before() {
		condition = new AttributeCondition(Attribute.AGILITY, 1);
	}

	@Test
	public void reason() {
		final Description expected = new Description.Builder("condition.attribute").name("attribute.agility").add("min", 1).build();
		assertEquals(expected, condition.reason());
	}

	@Test
	public void matches() {
		final Entity actor = mock(Entity.class);
		final EntityModel model = mock(EntityModel.class);
		final EnumerationIntegerMap<Attribute> attrs = new EnumerationIntegerMap<>(Attribute.class);
		when(model.attributes()).thenReturn(attrs);
		when(actor.model()).thenReturn(model);
		assertEquals(false, condition.matches(actor));
		attrs.get(Attribute.AGILITY).set(1);
		assertEquals(true, condition.matches(actor));
	}
}
