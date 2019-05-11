package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class CorpseTest {
	private Corpse corpse;
	private LootFactory butcher;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		butcher = mock(LootFactory.class);
		obj = create("object", 1).create();
		corpse = new Corpse(create("corpse", 2), Optional.of(butcher), List.of(obj));
	}

	private static ObjectDescriptor create(String name, int weight) {
		return new ObjectDescriptor.Builder(name).weight(weight).decay(Duration.ofMinutes(1)).build();
	}

	@Test
	public void constructor() {
		assertEquals("corpse", corpse.name());
		assertEquals(3, corpse.weight());
	}

	@Test
	public void butcher() throws ActionException {
		corpse.butcher(null);
		verify(butcher).generate(null);
	}

	@Test
	public void butcherAlreadyButchered() throws ActionException {
		corpse.butcher(null);
		TestHelper.expect("corpse.already.butchered", () -> corpse.butcher(null));
	}

	@Test
	public void butcherCannotButcher() throws ActionException {
		corpse = new Corpse(create("corpse", 0), Optional.empty(), List.of(obj));
		TestHelper.expect("corpse.cannot.butcher", () -> corpse.butcher(null));
	}

	@Test
	public void fixedContents() {
		// TODO
	}
}
