package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.object.ObjectController;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

public class CorpseControllerTest extends ActionTestBase {
	private CorpseController controller;
	private ObjectController decay;

	@BeforeEach
	public void before() {
		decay = mock(ObjectController.class);
		controller = new CorpseController(decay);
	}

	@Test
	public void create() {
		// Init race
		final Race race = new Race.Builder("race")
			.size(Size.MEDIUM)
			.weight(42)
			.corpse()
			.build();
		when(actor.descriptor().race()).thenReturn(race);

		// Add inventory
		final WorldObject obj = ObjectDescriptor.of("object").create();
		obj.parent(actor);

		// Create corpse
		final Corpse corpse = controller.create(actor);
		assertNotNull(corpse);

		// Check corpse
		assertEquals(loc, corpse.parent());
		assertEquals(true, corpse.isAlive());
		assertEquals("corpse.race", corpse.descriptor().name());

		// Check contents
		assertEquals(1, corpse.contents().size());
		assertEquals(obj, corpse.contents().stream().iterator().next());

		// Check decay event registered
		verify(decay).decay(corpse);
	}

	@Test
	public void createIncorporeal() {
		final Race race = new Race.Builder("race").build();
		when(actor.descriptor().race()).thenReturn(race);
		final Corpse corpse = controller.create(actor);
		assertEquals(null, corpse);
	}
}
