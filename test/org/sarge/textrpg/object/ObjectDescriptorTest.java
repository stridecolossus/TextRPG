package org.sarge.textrpg.object;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.util.Percentile;

public class ObjectDescriptorTest {
	private ObjectDescriptor descriptor;
	private Material mat;
	private Emission emission;
	
	@Before
	public void before() {
		emission = new Emission("emission", Emission.Type.ODOUR, Percentile.ONE);
		mat = new Material("mat", Collections.emptySet(), Collections.emptySet(), 0);
		descriptor = new Builder("object")
			// Properties
			.weight(1)
			.value(2)
			.size(Size.SMALL)
			.reset(3)
			.forget(4)
			// Characteristics
			.description("long")
			.colour("colour")
			.material(mat)
			.category("one")
			.category("two")
			.visibility(Percentile.HALF)
			.emission(emission)
			.quiet()
			// Equipment
			.slot(DeploymentSlot.ARMS)
			.twoHanded(false)
			.condition(Condition.TRUE)
			.armour(5)
			.passive(Effect.NONE)
			// Construct
			.build();
	}
	
	@Test
	public void constructor() {
		// Check object
		assertEquals("object", descriptor.getName());

		// Properties
		assertNotNull(descriptor.getProperties());
		assertEquals(1, descriptor.getProperties().getWeight());
		assertEquals(2, descriptor.getProperties().getValue());
		assertEquals(Size.SMALL, descriptor.getProperties().getSize());
		assertEquals(3L, descriptor.getProperties().getResetPeriod());
		assertEquals(4L, descriptor.getProperties().getForgetPeriod());
		
		// Check characteristics
		assertNotNull(descriptor.getCharacteristics());
		assertEquals("long", descriptor.getCharacteristics().getFullDescriptionKey());
		assertEquals("colour", descriptor.getCharacteristics().getColour());
		assertEquals(mat, descriptor.getCharacteristics().getMaterial());
		assertArrayEquals(new String[]{"one", "two"}, descriptor.getCharacteristics().getCategories().toArray());
		assertEquals(true, descriptor.isCategory("one"));
		assertEquals(true, descriptor.isCategory("two"));
		assertEquals(Percentile.HALF, descriptor.getCharacteristics().getVisibility());
		assertEquals(Optional.of(emission), descriptor.getCharacteristics().getEmission(Emission.Type.ODOUR));
		assertEquals(true, descriptor.getCharacteristics().isQuiet());
		
		// Check equipment
		assertNotNull(descriptor.getEquipment());
		assertEquals(Optional.of(DeploymentSlot.ARMS), descriptor.getEquipment().getDeploymentSlot());
		assertEquals(5, descriptor.getEquipment().getArmour());
		assertEquals(Effect.NONE, descriptor.getEquipment().getPassive());
	}
	
	@Test
	public void toFixture() {
		final ObjectDescriptor fixture = descriptor.toFixture();
		assertEquals(ObjectDescriptor.IMMOVABLE, fixture.getProperties().getWeight());
		assertEquals(descriptor, descriptor.toFixture());
	}
}
