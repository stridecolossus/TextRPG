package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class QuiverTest {
	private Quiver quiver;
	private Ammo ammo;

	@BeforeEach
	public void before() {
		quiver = new Quiver(new Quiver.Descriptor(ObjectDescriptor.of("quiver"), Ammo.Type.ARROW, 2));
		ammo = new Ammo.Descriptor(new ObjectDescriptor.Builder("ammo").weight(1).build(), Ammo.Type.ARROW, Damage.DEFAULT, Percentile.ONE).create();
	}

	@Test
	public void constructor() {
		assertNotNull(quiver.contents());
		assertEquals(true, quiver.contents().isEmpty());
		assertNotNull(quiver.iterator());
		assertEquals(false, quiver.iterator().hasNext());
	}

	@Test
	public void reasonNotAmmo() {
		assertEquals(Optional.of("quiver.invalid.object"), quiver.contents().reason(mock(Thing.class)));
	}

	@Test
	public void reasonIncorrectAmmo() {
		final Ammo bolt = new Ammo.Descriptor(ObjectDescriptor.of("bolt"), Ammo.Type.BOLT, Damage.DEFAULT, Percentile.ONE).create();
		assertEquals(Optional.of("quiver.invalid.ammo"), quiver.contents().reason(bolt));
	}

	@Test
	public void add() {
		assertEquals(Optional.empty(), quiver.contents().reason(ammo));
		ammo.parent(TestHelper.parent());
		ammo.parent(quiver);
		assertEquals(false, ammo.isAlive());
		assertEquals(1, quiver.contents().size());
		assertEquals(1, quiver.weight());
	}

	@Test
	public void addFull() {
		for(int n = 0; n < 2; ++n) {
			ammo.parent(TestHelper.parent());
			ammo.parent(quiver);
			ammo.modify(1);
		}
		assertEquals(Optional.of("quiver.full"), quiver.contents().reason(ammo));
	}

	@Test
	public void addStack() {
		// Add ammo of the same type
		for(int n = 0; n < 2; ++n) {
			ammo.parent(TestHelper.parent());
			ammo.parent(quiver);
			ammo.modify(1);
		}
		assertEquals(1, quiver.contents().size());
		assertEquals(2, quiver.weight());

		// Check stack
		final ObjectStack stack = (ObjectStack) quiver.contents().stream().iterator().next();
		assertEquals(ammo.descriptor(), stack.descriptor());
		assertEquals(2, stack.count());
	}

	@Test
	public void addDifferentDescriptors() {
		// Add ammo
		ammo.parent(TestHelper.parent());
		ammo.parent(quiver);

		// Add a different ammo
		final Ammo other = new Ammo.Descriptor(ObjectDescriptor.of("other"), Ammo.Type.ARROW, Damage.DEFAULT, Percentile.ONE).create();
		other.parent(TestHelper.parent());
		other.parent(quiver);

		// Check two stacks generated
		assertEquals(2, quiver.contents().size());
		assertEquals(1, quiver.weight());
	}

	@Test
	public void iterator() {
		// Add some ammo
		ammo.parent(TestHelper.parent());
		ammo.parent(quiver);

		// Check iterator
		final Iterator<Ammo.Descriptor> itr = quiver.iterator();
		assertNotNull(itr);
		assertEquals(true, itr.hasNext());

		// Consume ammo
		final Ammo.Descriptor result = itr.next();
		assertEquals(ammo.descriptor(), result);
		assertEquals(false, itr.hasNext());
		assertEquals(true, quiver.contents().isEmpty());
	}
}
