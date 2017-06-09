package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.*;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.util.Percentile;

public class WorldObjectTest extends ActionTest {
	private WorldObject obj;
	private ObjectDescriptor descriptor;
	private Emission light;
	
	@Before
	public void before() {
		light = new Emission(null, Emission.Type.LIGHT, Percentile.ONE);
		
		descriptor = new Builder("object")
			.weight(1)
			.value(2)
			.size(Size.SMALL)
			.material(new Material("wood", Collections.emptySet(), Collections.singleton(DamageType.FIRE), 3))
			.visibility(Percentile.HALF)
			.emission(light)
			.quiet()
			.description("full")
			.build();
		
		obj = new WorldObject(descriptor);
	}
	
	@Test
	public void constructor() {
		assertEquals("object", obj.getName());
		assertEquals(descriptor, obj.getDescriptor());
		assertEquals("object", descriptor.getDescriptionKey());
		assertEquals(1, obj.getWeight());
		assertEquals(2, obj.getValue());
		assertEquals(Percentile.HALF, obj.getVisibility());
		assertEquals(Optional.of(light), obj.getEmission(Emission.Type.LIGHT));
		assertEquals(Optional.empty(), obj.getOpenableModel());
		assertEquals(false, obj.isDamaged());
		assertEquals(false, obj.isBroken());
		assertEquals(false, obj.isSentient());
		assertEquals(true, obj.isDead());
		assertEquals(false, obj.isFixture());
		assertEquals(true, obj.isQuiet());
	}
	
	@Test
	public void describe() {
		final Description desc = obj.describe();
		assertNotNull(desc);
		assertEquals("description.full", desc.getKey());
		assertEquals("{cardinality.single}", desc.get("cardinality"));
		assertEquals("{size.small}", desc.get("size"));
	}

	@Test
	public void describeShort() {
		final Description desc = obj.describeShort();
		assertNotNull(desc);
		assertEquals("description.object", desc.getKey());
	}
	
	@Test
	public void take() throws ActionException {
		obj.take(actor);
	}
	
	@Test
	public void takeImmovable() throws ActionException {
		obj = new Builder("immovable").weight(ObjectDescriptor.IMMOVABLE).build().create();
		expect("take.immovable.object");
		obj.take(actor);
	}
	
	@Test
	public void takeAlreadyCarried() throws ActionException {
		obj.setParent(actor);
		expect("take.already.carried");
		obj.take(actor);
	}
	
	@Test
	public void takeNotAvailable() throws ActionException {
		final Parent parent = mock(Actor.class);
		when(parent.getContents()).thenReturn(new Contents());
		obj.setParent(parent);
		expect("take.cannot.take");
		obj.take(actor);
	}
	
	@Test
	public void isOwner() throws ActionException {
		final Actor actor = mock(Actor.class);
		when(actor.getContents()).thenReturn(new Contents());
		obj.setParent(actor);
		assertEquals(actor, obj.getOwner());
	}
	
	@Test
	public void damage() throws ActionException {
		// Add to parent (so not dead)
		final Parent parent = mock(Parent.class);
		when(parent.getContents()).thenReturn(new Contents());
		obj.setParent(parent);
		
		// Check damage resistance
		obj.damage(DamageType.COLD, 999);
		assertEquals(false, obj.isDead());
		
		// Check strength
		obj.damage(DamageType.FIRE, 1);
		assertEquals(false, obj.isDead());
		
		// Check destroyed by sufficient damage
		obj.damage(DamageType.FIRE, 999);
		assertEquals(true, obj.isDead());
	}
}
