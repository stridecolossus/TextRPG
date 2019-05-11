package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.Element.ElementException;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.ConditionLoader;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.RegistryLoader;

public class ObjectDescriptorLoaderTest {
	private ObjectDescriptorLoader loader;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		final RegistryLoader<Material> material = mock(RegistryLoader.class);
		loader = new ObjectDescriptorLoader(material, mock(ConditionLoader.class));
	}

	@Test
	public void load() {
		// Build XML
		final var xml = new Element.Builder("xml")
			// Properties
			.attribute("name", "object")
			.attribute("weight", 1)
			.attribute("size", Size.MEDIUM)
			.attribute("alignment", Alignment.EVIL)
			.attribute("value", 2)
			.attribute("two-handed", true)
			// Periods
			.attribute("reset", "3s")
			.attribute("decay", "5s")
			// Characteristics
			.attribute("cardinality", Cardinality.PAIR)
			.attribute("vis", "50")
			.attribute("quiet", true)
			.attribute("category", "cat")
			// Equipment
			.attribute("slot", Slot.MAIN)
			.attribute("armour", 6)
			.attribute("warmth", 7)
			.attribute("noise", 50)
			.build();

		// Load descriptor
		final var descriptor = loader.load(xml, ObjectDescriptorLoader.Policy.OBJECT);
		assertNotNull(descriptor);

		// Check descriptor
		assertEquals("object", descriptor.name());
		assertEquals(false, descriptor.isFixture());
		assertEquals(false, descriptor.isResetable());
		assertEquals(false, descriptor.isStackable());

		// Check properties
		final var props = descriptor.properties();
		assertNotNull(props);
		assertEquals(1, props.weight());
		assertEquals(Size.MEDIUM, props.size());
		assertEquals(Alignment.EVIL, props.alignment());
		assertEquals(2, props.value());
		assertEquals(true, props.isTwoHanded());

		// Check periods
		assertEquals(Duration.ofSeconds(3), props.reset());
		assertEquals(Duration.ofSeconds(5), props.decay());

		// Check characteristics
		final var chars = descriptor.characteristics();
		assertNotNull(chars);
		assertEquals(ObjectDescriptor.Characteristics.PLACEMENT_DEFAULT, chars.placement());
		assertEquals(Optional.empty(), chars.qualifier());
		assertEquals(Cardinality.PAIR, chars.cardinality());
		assertEquals(Percentile.HALF, chars.visibility());
		assertEquals(true, chars.isQuiet());
		assertEquals(Material.NONE, chars.material());

		// Check equipment
		final var equipment = descriptor.equipment();
		assertNotNull(equipment);
		assertEquals(Slot.MAIN, equipment.slot());
		assertEquals(6, equipment.armour());
		assertEquals(7, equipment.warmth());
		assertEquals(Percentile.HALF, equipment.noise());
	}

	@Test
	public void loadFixtureOnly() {
		final Element xml = new Element.Builder("object").attribute("name", "name").build();
		final ObjectDescriptor descriptor = loader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE);
		assertEquals(true, descriptor.isFixture());
	}

	@Test
	public void loadFixtureOnlyInvalidObject() {
		final Element xml = new Element.Builder("object").attribute("name", "name").attribute("fixture", false).build();
		assertThrows(ElementException.class, () -> loader.load(xml, ObjectDescriptorLoader.Policy.FIXTURE));
	}

	@Test
	public void loadObjectsOnly() {
		final Element xml = new Element.Builder("object").attribute("name", "name").build();
		final ObjectDescriptor descriptor = loader.load(xml, ObjectDescriptorLoader.Policy.OBJECT);
		assertEquals(false, descriptor.isFixture());
	}

	@Test
	public void loadObjectsOnlyInvalidFixture() {
		final Element xml = new Element.Builder("object").attribute("name", "name").attribute("fixture", true).build();
		assertThrows(ElementException.class, () -> loader.load(xml, ObjectDescriptorLoader.Policy.OBJECT));
	}
}
