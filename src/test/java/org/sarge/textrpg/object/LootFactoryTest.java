package org.sarge.textrpg.object;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Test;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.util.Percentile;

public class LootFactoryTest extends ActionTest {
	@Test
	public void object() {
		// Define loot-factory to generate a single object
		final ObjectDescriptor descriptor = new ObjectDescriptor("object");
		final LootFactory f = LootFactory.object(descriptor, 1);

		// Generate loot
		final Stream<WorldObject> loot = f.generate(actor);
		assertNotNull(loot);
		
		// Check loot generated
		final Iterator<WorldObject> itr = loot.iterator();
		assertEquals(true, itr.hasNext());
		
		// Check generated object
		final WorldObject obj = itr.next();
		assertEquals(descriptor, obj.getDescriptor());
		assertEquals(false, itr.hasNext());
	}
	
	@Test
	public void money() {
		final LootFactory f = LootFactory.money(Value.literal(42));
		assertArrayEquals(new WorldObject[]{new Money(42)}, f.generate(actor).toArray());
	}
	
	@Test
	public void chance() {
		// Create a factory that has zero chance of being invoked
		final LootFactory delegate = mock(LootFactory.class);
		final LootFactory no = LootFactory.chance(Percentile.ZERO, Value.literal(-999), delegate);
		no.generate(actor);
		verifyZeroInteractions(delegate);

		// Create a factory that will always be invoked
		final LootFactory yes = LootFactory.chance(Percentile.ZERO, null, delegate);
		yes.generate(actor);
		verify(delegate).generate(actor);
	}
}
