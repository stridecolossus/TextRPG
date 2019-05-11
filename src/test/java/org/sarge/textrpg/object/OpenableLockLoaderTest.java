package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.EffectLoader;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Openable.Lock;
import org.sarge.textrpg.common.Trap;
import org.sarge.textrpg.util.Percentile;

public class OpenableLockLoaderTest {
	private OpenableLockLoader loader;
	private EffectLoader effect;

	@BeforeEach
	public void before() {
		effect = mock(EffectLoader.class);
		loader = new OpenableLockLoader(effect);
	}

	@Test
	public void lock() {
		final Element xml = new Element.Builder("lock")
			.attribute("type", "lock")
			.attribute("key", "key")
			.attribute("pick", "50")
			.build();
		final Lock lock = loader.load(xml);
		final ObjectDescriptor key = new ObjectDescriptor.Builder("key").slot(Slot.KEYRING).build();
		final Lock expected = new Openable.Lock(key, Percentile.HALF, null);
		assertEquals(expected, lock);
	}

	@Test
	public void trapped() {
		final Element xml = new Element.Builder("lock")
			.attribute("type", "lock")
			.attribute("key", "key")
			.attribute("pick", "50")
			.child("trap")
				.attribute("diff", "50")
				.add("effect")
				.end()
			.build();
		when(effect.load(any(Element.class))).thenReturn(Effect.NONE);
		final Lock lock = loader.load(xml);
		final ObjectDescriptor key = new ObjectDescriptor.Builder("key").slot(Slot.KEYRING).build();
		final Lock expected = new Openable.Lock(key, Percentile.HALF, new Trap(Effect.NONE, Percentile.HALF));
		assertEquals(expected, lock);
	}

	@Test
	public void defaultLock() {
		final Element xml = new Element.Builder("lock").attribute("type", "default").build();
		final Lock lock = loader.load(xml);
		assertEquals(Openable.Lock.DEFAULT, lock);
	}

	@Test
	public void latch() {
		final Element xml = new Element.Builder("lock").attribute("type", "latch").build();
		final Lock lock = loader.load(xml);
		assertEquals(Openable.Lock.LATCH, lock);
	}

	@Test
	public void key() {
		final ObjectDescriptor key = loader.key("key");
		assertEquals(key, loader.key("key"));
	}
}
