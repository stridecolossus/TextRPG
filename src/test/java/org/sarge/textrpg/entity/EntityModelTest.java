package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.entity.EntityModel.AppliedEffect;
import org.sarge.textrpg.util.EnumerationIntegerMap;
import org.sarge.textrpg.util.ValueModifier;

public class EntityModelTest {
	private EntityModel model;

	@BeforeEach
	public void before() {
		model = new EntityModel("name", new EnumerationIntegerMap<>(Attribute.class));
	}

	@Test
	public void constructor() {
		assertNotNull(model.values());
		assertEquals(Stance.DEFAULT, model.stance());
		assertEquals(Group.NONE, model.group());
		assertNotNull(model.trail());
		assertNotNull(model.effects());
		assertEquals(0, model.effects().count());
	}

	@Test
	public void modifierEntityValue() {
		final var mod = model.modifier(EntityValue.ARMOUR.key());
		assertNotNull(mod);
		mod.modify(42);
		assertEquals(42, model.values().get(EntityValue.ARMOUR.key()).get());
	}

	@Test
	public void modifierAttribute() {
		final var mod = model.modifier(Attribute.AGILITY);
		assertNotNull(mod);
		mod.modify(42);
		assertEquals(42, model.attributes().get(Attribute.AGILITY).get());
	}

	@Test
	public void modifierInvalid() {
		assertThrows(IllegalArgumentException.class, () -> model.modifier(null));
	}

	@Test
	public void applied() {
		final AppliedEffect applied = model.new AppliedEffect("name", mock(ValueModifier.class), 42, Effect.Group.DEFAULT);
		assertArrayEquals(new AppliedEffect[]{applied}, model.effects().toArray());
	}

	@Test
	public void remove() {
		final ValueModifier mod = mock(ValueModifier.class);
		final AppliedEffect applied = model.new AppliedEffect("name", mod, 42, Effect.Group.DEFAULT);
		applied.remove();
		verify(mod).modify(-42);
		assertEquals(0, model.effects().count());
	}

	@Test
	public void stance() {
		model.stance(Stance.SNEAKING);
		assertEquals(Stance.SNEAKING, model.stance());
	}

	@Test
	public void group() {
		final Group group = mock(Group.class);
		model.group(group);
		assertEquals(group, model.group());
	}
}
