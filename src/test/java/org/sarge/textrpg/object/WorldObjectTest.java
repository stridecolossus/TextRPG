package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.TrackedContents;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.util.TextHelper;

public class WorldObjectTest {
	private WorldObject obj;

	@BeforeEach
	public void before() {
		final Material mat = new Material.Builder("mat").strength(1).damaged(Damage.Type.COLD).build();
		obj = new ObjectDescriptor.Builder("object")
			.weight(1)
			.value(2)
			.size(Size.MEDIUM)
			.decay(Duration.ofMinutes(1))
			.reset(Duration.ofMinutes(2))
			.visibility(Percentile.ONE)
			.material(mat)
			.category("cat")
			.build()
			.create();
	}

	@Test
	public void constructor() {
		assertEquals("object", obj.name());
		assertNotNull(obj.descriptor());
		assertEquals(false, obj.isAlive());
		assertEquals(1, obj.weight());
		assertEquals(1, obj.count());
		assertEquals(2, obj.value());
		assertEquals(Size.MEDIUM, obj.size());
		assertEquals(Percentile.ONE, obj.visibility());
		assertEquals(Percentile.ZERO, obj.emission(Emission.LIGHT));
		assertEquals(false, obj.isDamaged());
		assertEquals(false, obj.isBroken());
	}

	@Test
	public void isCategory() {
		assertEquals(true, obj.isCategory("cat"));
		assertEquals(false, obj.isCategory("cobblers"));
	}

	@Test
	public void isCarried() {
		assertEquals(false, obj.isCarried());
		final Parent parent = mock(Parent.class);
		when(parent.isSentient()).thenReturn(true);
		when(parent.contents()).thenReturn(new Contents());
		obj.parent(parent);
		assertEquals(true, obj.isCarried());
	}

	@Test
	public void placement() {
		assertEquals("carried", obj.key(true));
		assertEquals("dropped", obj.key(false));
	}

	@Test
	public void placementContained() {
		final Container container = mock(Container.class);
		when(container.contents()).thenReturn(new Contents());
		obj.parent(container);
		assertEquals("contained", obj.key(false));
	}

	@Test
	public void describe() {
		final Description expected = new Description.Builder("object.dropped")
			.add("name", "object")
			.add("size", TextHelper.prefix(Size.MEDIUM))
			.add("cardinality", TextHelper.prefix(Cardinality.SINGLE))
			.add("placement", "placement.default")
			.add(WorldObject.KEY_STATE, StringUtils.EMPTY)
			.add(WorldObject.KEY_CONDITION, StringUtils.EMPTY)
			.build();
		assertEquals(expected, obj.describe(null));
	}

	@Test
	public void damage() {
		final Parent parent = mock(Parent.class);
		when(parent.contents()).thenReturn(new Contents());
		obj.parent(parent);
		obj.damage(Damage.Type.COLD, 1);
		assertEquals(false, obj.isAlive());
	}

	@Test
	public void damageIgnored() {
		obj.parent(TestHelper.parent());
		obj.damage(Damage.Type.CRUSHING, 999);
		assertNotNull(obj.parent());
	}

	@ParameterizedTest
	@EnumSource(value=Interaction.class, mode=EnumSource.Mode.EXCLUDE, names={"PUSH", "PULL", "EXAMINE"})
	public void interactionInvert(Interaction interaction) {
		assertEquals(interaction, interaction.invert());
	}

	@Test
	public void interactionInvertSpecial() {
		assertEquals(Interaction.PULL, Interaction.PUSH.invert());
		assertEquals(Interaction.PUSH, Interaction.PULL.invert());
		assertThrows(IllegalStateException.class, () -> Interaction.EXAMINE.invert());
	}

	@Test
	public void filter() {
		final WorldObject.Filter filter = WorldObject.Filter.of(ObjectDescriptor.Filter.of(obj.descriptor()));
		assertEquals(true, filter.test(obj));
		assertEquals(false, filter.test(mock(WorldObject.class)));
	}

	@Test
	public void categoryFilter() {
		final WorldObject.Filter filter = WorldObject.Filter.of("cat");
		assertEquals(true, filter.test(obj));
		assertEquals(false, filter.test(mock(WorldObject.class)));
	}

	@Test
	public void decay() {
		final Parent parent = mock(Parent.class);
		when(parent.contents()).thenReturn(new TrackedContents());
		obj.parent(parent);
		obj.decay();
		assertEquals(false, obj.isAlive());
		verify(parent).notify(ContentStateChange.of(ContentStateChange.Type.CONTENTS, new Description("object.decayed", "object")));
	}

	@Test
	public void decayZeroWeight() {
		final Parent parent = mock(Parent.class);
		when(parent.contents()).thenReturn(new TrackedContents());
		obj = ObjectDescriptor.of("object").create();
		obj.parent(parent);
		obj.decay();
		assertEquals(false, obj.isAlive());
		verify(parent, never()).notify(any());
	}

	@Test
	public void decayIgnoreCarried() {
		final Parent parent = mock(Parent.class);
		when(parent.isSentient()).thenReturn(true);
		when(parent.contents()).thenReturn(new Contents());
		obj.parent(parent);
		obj.decay();
		assertEquals(true, obj.isAlive());
		verify(parent, never()).notify(any());
	}

	@Test
	public void destroy() {
		obj.parent(TestHelper.parent());
		obj.destroy();
		assertEquals(false, obj.isAlive());
	}

	@Test
	public void destroyFixture() {
		obj = ObjectDescriptor.fixture("fixture").create();
		obj.parent(TestHelper.parent());
		assertThrows(AssertionError.class, () -> obj.destroy());
	}
}
