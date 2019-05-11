package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.object.ObjectDescriptor.Filter;
import org.sarge.textrpg.object.ObjectDescriptor.PassiveEffect;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.ValueModifier;

public class ObjectDescriptorTest {
	@Test
	public void create() {
		final ObjectDescriptor descriptor = ObjectDescriptor.of("name");
		assertEquals(new ObjectDescriptor.Builder("name").build(), descriptor);
	}

	@Test
	public void fixture() {
		final ObjectDescriptor fixture = ObjectDescriptor.fixture("fixture");
		assertEquals(true, fixture.isFixture());
	}

	@Test
	public void builder() {
		// Create dependencies
		final Material mat = new Material.Builder("mat").build();
		final PassiveEffect passive = new PassiveEffect(mock(ValueModifier.Key.class), 42);

		// Build descriptor
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("name")
			// Properties
			.weight(1)
			.size(Size.MEDIUM)
			.alignment(Alignment.EVIL)
			.twoHanded()
			.value(2)
			.reset(Duration.ofMinutes(3))
			.decay(Duration.ofMinutes(5))
			// Characteristics
			.placement("key")
			.qualifier("qualifier")
			.cardinality(Cardinality.PAIR)
			.material(mat)
			.category("cat")
			.visibility(Percentile.HALF)
			.quiet(true)
			// Equipment
			.slot(Slot.MAIN)
			.condition(Condition.TRUE)
			.armour(6)
			.warmth(7)
			.noise(Percentile.HALF)
			.passive(passive)
			.build();

		// Check descriptor
		assertNotNull(descriptor);
		assertEquals("name", descriptor.name());
		assertEquals(false, descriptor.isFixture());
		assertEquals(false, descriptor.isResetable());

		// Check properties
		assertNotNull(descriptor.properties());
		assertEquals(1, descriptor.properties().weight());
		assertEquals(Size.MEDIUM, descriptor.properties().size());
		assertEquals(Alignment.EVIL, descriptor.properties().alignment());
		assertEquals(true, descriptor.properties().isTwoHanded());
		assertEquals(2, descriptor.properties().value());
		assertEquals(Duration.ofMinutes(3), descriptor.properties().reset());
		assertEquals(Duration.ofMinutes(5), descriptor.properties().decay());

		// Check characteristics
		assertNotNull(descriptor.characteristics());
		assertEquals("key", descriptor.characteristics().placement());
		assertEquals(Optional.of("qualifier"), descriptor.characteristics().qualifier());
		assertEquals(Cardinality.PAIR, descriptor.characteristics().cardinality());
		assertEquals(mat, descriptor.characteristics().material());
		assertEquals(Percentile.HALF, descriptor.characteristics().visibility());
		assertEquals(true, descriptor.characteristics().isQuiet());
		assertEquals(Set.of("cat"), descriptor.characteristics().categories());

		// Check equipment
		assertNotNull(descriptor.equipment());
		assertEquals(Slot.MAIN, descriptor.equipment().slot());
		assertEquals(6, descriptor.equipment().armour());
		assertEquals(7, descriptor.equipment().warmth());
		assertNotNull(descriptor.equipment().conditions());
		assertEquals(Percentile.HALF, descriptor.equipment().noise());
		assertEquals(List.of(passive), descriptor.equipment().passive());
	}

	@Test
	public void resetableMissingResetPeriod() {
		class InvalidResetable extends ObjectDescriptor {
			public InvalidResetable() {
				super(ObjectDescriptor.of("resetable"));
			}

			@Override
			public boolean isResetable() {
				return true;
			}
		}
		assertThrows(IllegalArgumentException.class, () -> new InvalidResetable());
	}

	@Test
	public void twoHandedInvalidDeploymentSlot() {
		final Executable ctor = () -> new ObjectDescriptor.Builder("two").twoHanded().slot(Slot.BELT).build();
		assertThrows(IllegalArgumentException.class, ctor);
	}

	@Test
	public void stackableResetable() {
		final Executable ctor = () -> new ObjectDescriptor(ObjectDescriptor.of("stack")) {
			@Override
			public boolean isResetable() {
				return true;
			}
		};
		assertThrows(IllegalArgumentException.class, ctor);
	}

	@Test
	public void stackableFixture() {
		final Executable ctor = () -> new ObjectDescriptor(ObjectDescriptor.of("stack")) {
			@Override
			public boolean isStackable() {
				return true;
			}

			@Override
			public boolean isFixture() {
				return true;
			}
		};
		assertThrows(IllegalArgumentException.class, ctor);
	}

	@Test
	public void buildNotEquipment() {
		new ObjectDescriptor.Builder("name").slot(Slot.NONE).build();
	}

	@Test
	public void buildEquipmentPropertiesWithoutSlot() {
		assertThrows(IllegalArgumentException.class, () -> new ObjectDescriptor.Builder("name").armour(42).build());
	}

	@Test
	public void filterAll() {
		assertEquals(true, Filter.ALL.test(ObjectDescriptor.of("any")));
	}

	@Test
	public void filterMatchingDescriptor() {
		final ObjectDescriptor descriptor = ObjectDescriptor.of("descriptor");
		assertEquals(true, Filter.of(descriptor).test(descriptor));
		assertEquals(false, Filter.of(ObjectDescriptor.of("other")).test(descriptor));
	}

	@Test
	public void filterDeploymentSlot() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("arms").slot(Slot.ARMS).build();
		assertEquals(true, Filter.slot(Slot.ARMS).test(descriptor));
		assertEquals(false, Filter.slot(Slot.HEAD).test(descriptor));
	}

	@Test
	public void filterOneTwoHanded() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("two-handed").twoHanded().build();
		assertEquals(false, Filter.ONE_HANDED.test(descriptor));
		assertEquals(true, Filter.TWO_HANDED.test(descriptor));
	}
}
